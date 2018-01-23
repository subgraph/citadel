/* #define _GNU_SOURCE	** for vasprintf() */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <stdarg.h>
#include <errno.h>
#include <assert.h>

#define MAX_LINE 1000
#define MAX_PKGS 100

char *checksum_field=NULL;

/* exclude_field is a comma-separated list of dependencies that you want to exclude, for example:
   Package: init
   Pre-Depends: systemd-sysv | sysvinit-core | upstart

   This will always give systemd-sysv with GETDEPS, unless you use
   DEBOOTSTRAP_EXCLUDE_FIELD=systemd-sysv,sysvinit-core pkgdetails Packages init
 */
char *exclude_field=NULL;
char **exclude_array=NULL;

static void oom_die(void)
{
    fputs("Out of memory!\n", stderr);
    exit(1);
}

static char *xvasprintf(const char *fmt, va_list ap) {
    char *ret;

    if (vasprintf (&ret, fmt, ap) < 0) {
        if (errno == ENOMEM)
            oom_die();
        return NULL;
    }
    return ret;
}

static char *xasprintf(const char *fmt, ...) {
    va_list ap;
    char *ret;

    va_start(ap, fmt);
    ret = xvasprintf(fmt, ap);
    va_end(ap);
    return ret;
}

static void parse_exclude_field(const char *str) {
    char *workptr, *saveptr = NULL, *tok = NULL;
    workptr = strdup(str);
    int nf=0,f;
    tok=workptr;
    while(*tok==',') tok++;
    do {
      saveptr=strchr(tok,',');
      tok++;
      if (saveptr) tok=saveptr+1;
      nf++;
    } while(saveptr);

    exclude_array = (char **) malloc((nf+1) * sizeof(char *));
    if (exclude_array==NULL){
      oom_die();
    }
    tok = strtok_r(workptr, ",", &saveptr);
    for(f=0;f<nf;f++){
      exclude_array[f] = strdup(tok);
      tok = strtok_r(NULL, ",", &saveptr);
    }
    exclude_array[nf+1-1]=NULL;
    free(workptr);
}

static void free_exclude_array(void) {
    int f;
    if (exclude_array==NULL) return;
    f=0;
    while(exclude_array[f]!=NULL){
      free(exclude_array[f]);
      exclude_array[f]=NULL;
      f++;
    }
    free(exclude_array);
    exclude_array=NULL;
}

/* versionclause is not used at the moment */
static int exclude_dep(const char *dep, const char *versionclause) {
    int f;
    if (exclude_array==NULL) return(0);

    f=0;
    while(exclude_array[f]){
      if (!strcmp(exclude_array[f], dep)){
        return(1);
      }
      f++;
    }
    return(0);
}

static char *fieldcpy(char *dst, char *fld) {
    while (*fld && *fld != ':') 
        fld++;
    if (!*(fld++)) 
        return NULL;
    while (isspace(*fld)) fld++;
    return strcpy(dst, fld);
}

static void outputdeps(char *deps) {
    char *pch = deps;

    while (1) {
        while (isspace(*pch)) pch++;
        if (!*pch) break;

        while (*pch && *pch != '(' && *pch != '|' && *pch != ','
               && !isspace(*pch))
        {
            fputc(*pch++, stdout);
        }
        fputc('\n', stdout);
        while (*pch && *pch++ != ',') (void)NULL;
    }
}

typedef struct deps_proto {
  char *cur_pkg;
  int nd;
/* 95% of Depends: in jessie have less than 16 entries, for the rest
   we do a realloc() when necessary. */
#define DEPCHUNK	16
  char ** deps;
} deps_t, *deps_p;

static deps_p deps_new(void) {
  deps_p n = (deps_p) malloc(sizeof(deps_t));
  if (n==NULL) oom_die();
  memset(n, 0x00, sizeof(deps_t));

  n->cur_pkg = NULL;
  n->nd = 0;
  n->deps = NULL;

  return(n);
}

static void deps_free(deps_p d) {
  int i;
  if (d==NULL) return;
  for(i=0;i<d->nd;i++){
    if (d->deps[i]) {
      free(d->deps[i]);
      d->deps[i] = NULL;
    }
  }
  if (d->nd) {
    free(d->deps);
    d->deps = NULL;
  }
  d->nd = 0;
  if (d->cur_pkg) {
    free(d->cur_pkg);
    d->cur_pkg = NULL;
  }
  free(d);
}

static void deps_setpkgname(deps_p d, const char *cur_pkg) {
  if (d->cur_pkg) free(d->cur_pkg);
  d->cur_pkg = strdup(cur_pkg);
}

static void deps_add_4(deps_p d, const char *dep) {
  int di;

  /* Typically, a package depends on 0-10 other ones. I can't be
     bothered to write a sort function for that. Slowsort it is. */
  for (di=0;di<d->nd;di++){
    if (!strcmp(d->deps[di], dep)) return;
  }

  /* extend if necessary */
  if ((d->nd % DEPCHUNK) == 0) {
    d->deps = (char **) realloc(d->deps, (d->nd + DEPCHUNK) * sizeof(char *));
    if (d->deps == NULL) oom_die();
    for(di=0;di < DEPCHUNK;di++) d->deps[d->nd + di] = NULL;
  }

  /* add it */
  d->deps[d->nd] = strdup(dep);
  if (d->deps[d->nd] == NULL) oom_die();
  d->nd++;
}

static int deps_add_3(deps_p d, const char *versioneddep) {
  char workptr[MAX_LINE+1], *tok3, *saveptr3, *versionclause;
  int used;
  strcpy(workptr,versioneddep);
  tok3 = strtok_r(workptr, " (", &saveptr3);

  versionclause = strtok_r(NULL, " (", &saveptr3);
  /* versionclause is not used, but keep lint happy */
  if (versionclause) {
    while(*versionclause == ' ') versionclause++;
  }

  used = !exclude_dep(tok3, versionclause);

  if (!used) {
    fprintf(stderr,"W: pkgdetails: skip %s dependency on %s\n", d->cur_pkg, tok3);
  } else {
    /* add it to the list */
    deps_add_4(d, tok3);
  }

  return(used);
}

static void deps_add_2(deps_p d, const char *depsaltlist) {
  char workptr[MAX_LINE+1], *tok2, *saveptr2;
  int used, chose_alternative;

  /* shortcut: most dependencies don't have the alternatives syntax */
  if (strchr(depsaltlist, '|')==NULL) {
    (void) deps_add_3(d, depsaltlist);
    return;
  }

  /* parse this dependency-alternatives-list A | B | C */
  strcpy(workptr, depsaltlist);
  tok2 = strtok_r(workptr, " |", &saveptr2);
  chose_alternative = 0;
  used = 0;
  while(tok2){
    used = deps_add_3(d, tok2);
    if (used) break;  // we want only the first alternative from the list
    tok2 = strtok_r(NULL, " |", &saveptr2);
    if (tok2) {
      fprintf(stderr,"I: pkgdetails: consider %s dependency on %s\n", d->cur_pkg, tok2);
      chose_alternative = 1;
    }
  }

  if (! used) {
    fprintf(stderr,"E: pkgdetails: none of the %s dependencies chosen from alternatives %s\n", d->cur_pkg, depsaltlist);
  } else {
    if (chose_alternative) {
      fprintf(stderr,"I: pkgdetails: used %s dependency on %s\n", d->cur_pkg, tok2);
    }
  }
}

static void deps_add(deps_p d, const char *depslist) {
  char workptr[MAX_LINE+1], *tok, *saveptr = NULL;

  /* skip initial space */
  while ((depslist[0])&&(isspace(depslist[0]))) depslist++;

  assert(strlen(depslist) <= MAX_LINE);
  strcpy(workptr, depslist);
  tok = strtok_r(workptr, ",", &saveptr);
  while(tok){
    deps_add_2(d, tok);
    tok = strtok_r(NULL, ",", &saveptr);
  }
}

static void deps_output(FILE *f, const deps_p d) {
  int i;
  if ((f==NULL)||(d==NULL)||(d->nd==0)) return;

  for(i=0;i<d->nd;i++){
    fputs(d->deps[i], f);
    fputs("\n", f);
  }
}

/* The syntax of a Debian Depends: or Pre-Depends: line is as follows:

   Depends: deps
   Pre-Depends: deps
   deps = dep | deps "," dep
   dep = versioneddep | altdep
   altdep = versioneddep "|" versioneddep | versioneddep "|" altdep
   versioneddep = simpledep | simpledep "(" compareclause ")"
   simpledep = packagename

   It needs to be parsed accurately to properly do alternatives, if we
   don't like the first choice of an altdep list (hint: init)
 */
static void dogetdeps(char *pkgsfile, char **in_pkgs, int pkgc) {
    char buf[MAX_LINE+1];
    char cur_pkg[MAX_LINE];
    char cur_deps[MAX_LINE];
    char cur_predeps[MAX_LINE];
    char prev_pkg[MAX_LINE];
    char *pkgs[MAX_PKGS];
    int i,l;
    int skip;
    FILE *f;
    int output_pkg = -1;
    deps_p curdeps = NULL;

    buf[MAX_LINE+1-1] = '\0';
    cur_pkg[0] = cur_deps[0] = cur_predeps[0] = prev_pkg[0] = '\0';

    for (i = 0; i < pkgc; i++) pkgs[i] = in_pkgs[i];

    f = fopen(pkgsfile, "r");
    if (f == NULL) {
        perror(pkgsfile);
        exit(1);
    }

    curdeps = deps_new();

    skip = 1;
    while (fgets(buf, MAX_LINE, f)) {
        l = strlen(buf);
        if (*buf && buf[l-1] == '\n') buf[l-1] = '\0';
        if (strncasecmp(buf, "Package:", 8) == 0) {
            int any = 0;
            skip = 1;
            fieldcpy(cur_pkg, buf);
            deps_setpkgname(curdeps, cur_pkg);
            if (strcmp(cur_pkg, prev_pkg) != 0) {
                if (output_pkg != -1)
                    pkgs[output_pkg] = NULL;
                if (cur_deps[0])
                    outputdeps(cur_deps);
                if (cur_predeps[0])
                    outputdeps(cur_predeps);
                strcpy(prev_pkg, cur_pkg);
            }
            cur_deps[0] = cur_predeps[0] = '\0';
            output_pkg = -1;
	    for (i = 0; i < pkgc; i++) {
		if (!pkgs[i]) continue;
		any = 1;
                if (strcmp(cur_pkg, pkgs[i]) == 0) {
                    skip = 0;
                    output_pkg = i;
                    break;
                }
            }
            if (!any) break;
        } else if (!skip && strncasecmp(buf, "Depends:", 8) == 0)
{
		deps_add(curdeps, &buf[8]);
}
        else if (!skip && strncasecmp(buf, "Pre-Depends:", 12) == 0)
{
		deps_add(curdeps, &buf[12]);
}
    }

    deps_output(stdout, curdeps);

    fclose(f);

    deps_free(curdeps);
}

static void dopkgmirrorpkgs(int uniq, char *mirror, char *pkgsfile, 
        char *fieldname, char **in_pkgs, int pkgc) 
{
    char buf[MAX_LINE+1];
    char cur_field[MAX_LINE];
    char cur_pkg[MAX_LINE];
    char cur_ver[MAX_LINE];
    char cur_arch[MAX_LINE];
    char cur_size[MAX_LINE];
    char cur_checksum[MAX_LINE];
    char cur_filename[MAX_LINE];
    char prev_pkg[MAX_LINE];
    char *pkgs[MAX_PKGS];
    int i,l;
    FILE *f;
    char *output = NULL;
    int output_pkg = -1;

    buf[MAX_LINE+1-1] = '\0';
    cur_field[0] = cur_pkg[0] = cur_ver[0] = cur_arch[0] = cur_filename[0] = prev_pkg[0] = '\0';

    for (i = 0; i < pkgc; i++) pkgs[i] = in_pkgs[i];

    f = fopen(pkgsfile, "r");
    if (f == NULL) {
        perror(pkgsfile);
        exit(1);
    }
    while (fgets(buf, MAX_LINE, f)) {
        l = strlen(buf);
        if (*buf && buf[l-1] == '\n') buf[l-1] = '\0';
        if (strncasecmp(buf, fieldname, strlen(fieldname)) == 0) {
            fieldcpy(cur_field, buf);
	}
        if (strncasecmp(buf, "Package:", 8) == 0) {
            fieldcpy(cur_pkg, buf);
            if (strcmp(cur_pkg, prev_pkg) != 0) {
                if (output)
                    fputs(output, stdout);
                if (uniq && output_pkg != -1)
                    pkgs[output_pkg] = NULL;
                strcpy(prev_pkg, cur_pkg);
            }
            free(output);
            output = NULL;
            output_pkg = -1;
        } else if (strncasecmp(buf, "Version:", 8) == 0) {
            fieldcpy(cur_ver, buf);
        } else if (strncasecmp(buf, "Architecture:", 13) == 0) {
            fieldcpy(cur_arch, buf);
        } else if (strncasecmp(buf, "Size:", 5) == 0) {
            fieldcpy(cur_size, buf);
        } else if (strncasecmp(buf, checksum_field, strlen(checksum_field)) == 0
	           && buf[strlen(checksum_field)] == ':') {
            fieldcpy(cur_checksum, buf);
        } else if (strncasecmp(buf, "Filename:", 9) == 0) {
            fieldcpy(cur_filename, buf);
        } else if (!*buf) {
	    int any = 0;
	    for (i = 0; i < pkgc; i++) {
		if (!pkgs[i]) continue;
		any = 1;
                if (strcmp(cur_field, pkgs[i]) == 0) {
                    free(output);
                    output = xasprintf("%s %s %s %s %s %s %s\n", cur_pkg, cur_ver, cur_arch, mirror, cur_filename, cur_checksum, cur_size);
                    output_pkg = i;
		    break;
		}
            }
	    if (!any) break;
            cur_field[0] = '\0';
        }
    }
    if (output)
        fputs(output, stdout);
    if (uniq && output_pkg != -1)
        pkgs[output_pkg] = NULL;
    fclose(f);

    /* any that weren't found are returned as "pkg -" */
    if (uniq) {
        for (i = 0; i < pkgc; i++) {
            if (pkgs[i]) {
                printf("%s -\n", pkgs[i]);
            }
        }
    }

}

static void dopkgstanzas(char *pkgsfile, char **pkgs, int pkgc)
{
    char buf[MAX_LINE+1];
    char *accum;
    size_t accum_size = 0, accum_alloc = MAX_LINE * 2;
    char cur_pkg[MAX_LINE];
    FILE *f;
    int l;

    buf[MAX_LINE+1-1] = '\0';
    accum = malloc(accum_alloc);
    if (!accum)
        oom_die();

    f = fopen(pkgsfile, "r");
    if (f == NULL) {
        perror(pkgsfile);
        free(accum);
        exit(1);
    }
    while (fgets(buf, MAX_LINE, f)) {
        if (*buf) {
	    size_t len = strlen(buf);
            if (accum_size + len + 1 > accum_alloc) {
                accum_alloc = (accum_size + len + 1) * 2;
                accum = realloc(accum, accum_alloc);
                if (!accum)
                    oom_die();
            }
            strcpy(accum + accum_size, buf);
	    accum_size += len;
        }
        l = strlen(buf);
        if (*buf && buf[l-1] == '\n') buf[l-1] = '\0';
        if (strncasecmp(buf, "Package:", 8) == 0) {
            fieldcpy(cur_pkg, buf);
        } else if (!*buf) {
            int i;
            for (i = 0; i < pkgc; i++) {
                if (!pkgs[i]) continue;
                if (strcmp(cur_pkg, pkgs[i]) == 0) {
                    fputs(accum, stdout);
		    if (accum[accum_size - 1] != '\n')
			fputs("\n\n", stdout);
		    else if (accum[accum_size - 2] != '\n')
			fputc('\n', stdout);
                    break;
                }
            }
            *accum = '\0';
            accum_size = 0;
        }
    }
    fclose(f);

    free(accum);
}

static int dotranslatewgetpercent(int low, int high, int end, char *str) {
    int ch;
    int val, lastval;
    int allow_percentage;

    /* print out anything that looks like a % on its own line, appropriately
     * scaled */

    lastval = val = 0;
    allow_percentage = 0;
    while ( (ch = getchar()) != EOF ) {
	if (isspace(ch)) {
	    allow_percentage = 1;
	} else if (allow_percentage && isdigit(ch)) {
	    val *= 10; val += ch - '0';
	} else if (allow_percentage && ch == '%') {
	    float f = (float) val / 100.0 * (high - low) + low;
	    if (str) {
	    	printf("P: %d %d %s\n", (int) f, end, str);
	    } else {
	    	printf("P: %d %d\n", (int) f, end);
	    }
	    lastval = val;
	} else {
	    val = 0;
	    allow_percentage = 0;
	}
    }
    return lastval == 100;
}

int main(int argc, char *argv[]) {
    checksum_field=getenv("DEBOOTSTRAP_CHECKSUM_FIELD");
    if (checksum_field == NULL) {
        checksum_field="MD5sum";
    }

    exclude_field=getenv("DEBOOTSTRAP_EXCLUDE_FIELD");
    if (exclude_field != NULL) {
       parse_exclude_field(exclude_field);
    } else {
       exclude_array = NULL;
    }

    if ((argc == 6 || argc == 5) && strcmp(argv[1], "WGET%") == 0) {
	if (dotranslatewgetpercent(atoi(argv[2]), atoi(argv[3]), 
	                           atoi(argv[4]), argc == 6 ? argv[5] : NULL))
	{
	    exit(0);
	} else {
	    exit(1);
	}
    } else if (argc >= 4 && strcmp(argv[1], "GETDEPS") == 0) {
        int i;
        for (i = 3; argc - i > MAX_PKGS; i += MAX_PKGS) {
	    dogetdeps(argv[2], argv+i, MAX_PKGS);
	}
	dogetdeps(argv[2], argv+i, argc-i);
        free_exclude_array();
	exit(0);
    } else if (argc >= 5 && strcmp(argv[1], "PKGS") == 0) {
        int i;
        for (i = 4; argc - i > MAX_PKGS; i += MAX_PKGS) {
	    dopkgmirrorpkgs(1, argv[2], argv[3], "Package:", argv+i, MAX_PKGS);
	}
	dopkgmirrorpkgs(1, argv[2], argv[3], "Package:", argv+i, argc-i);
	exit(0);
    } else if (argc >= 6 && strcmp(argv[1], "FIELD") == 0) {
        int i;
        for (i = 5; argc - i > MAX_PKGS; i += MAX_PKGS) {
	    dopkgmirrorpkgs(0, argv[3], argv[4], argv[2], argv+i, MAX_PKGS);
	}
	dopkgmirrorpkgs(0, argv[3], argv[4], argv[2], argv+i, argc-i);
	exit(0);
    } else if (argc >= 4 && strcmp(argv[1], "STANZAS") == 0) {
	int i;
	for (i = 3; argc - i > MAX_PKGS; i += MAX_PKGS) {
	    dopkgstanzas(argv[2], argv+i, MAX_PKGS);
	}
	dopkgstanzas(argv[2], argv+i, argc-i);
	exit(0);
    } else {
        fprintf(stderr, "usage: %s PKGS mirror packagesfile pkgs..\n", argv[0]);
        fprintf(stderr, "   or: %s FIELD field mirror packagesfile pkgs..\n", 
                argv[0]);
        fprintf(stderr, "   or: %s GETDEPS packagesfile pkgs..\n", argv[0]);
        fprintf(stderr, "   or: %s STANZAS packagesfile pkgs..\n", argv[0]);
	fprintf(stderr, "   or: %s WGET%% low high end reason\n", argv[0]);
        exit(1);
    }
}
