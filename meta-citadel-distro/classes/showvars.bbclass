
addtask showvars

do_showvars[nostamp] = "1"

python do_showvars() {
    # emit only the metadata that are variables but not functions
    isfunc = lambda key: bool(d.getVarFlag(key, 'func'))
    vars = sorted((key for key in bb.data.keys(d) if not key.startswith('__')))
    for var in vars:
        if not isfunc(var):
            try:
                val = d.getVar(var, True)
            except Exception as exc:
                bb.plain('Expansion of %s threw %s: %s' % (var, exc.__class__.__name__, str(exc)))
            bb.plain('%s="%s"' % (var,val))
}
