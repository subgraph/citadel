

## Very basic build instructions

    $ source setup-build-env
    $ bitbake citadel-image

## Git subtrees included

    $ git subtree add --prefix poky git://git.yoctoproject.org/poky.git master --squash
    $ git subtree add --prefix meta-intel  git://git.yoctoproject.org/meta-intel.git master --squash
    $ git gc && git prune

* `https://www.atlassian.com/blog/git/alternatives-to-git-submodule-git-subtree`
* `https://legacy-developer.atlassian.com/blog/2015/05/the-power-of-git-subtree/`
