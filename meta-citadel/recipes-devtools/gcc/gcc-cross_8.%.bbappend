
# headers needed to compile gcc plugins, needed for grsec gcc plugins during kernel build
do_install_append () {
	cp ${S}/include/libiberty.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/mem-stats-traits.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/hash-traits.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/hash-map-traits.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/mem-stats.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/memory-block.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/config/i386/linux64.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/config/i386
	cp ${S}/gcc/brig-builtins.def ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/expr.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/tree-vrp.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/builtins.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/backend.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/poly-int.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/poly-int-types.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/
	cp ${S}/gcc/wide-int-bitmask.h ${D}${libdir}/gcc/${TARGET_SYS}/${BINV}/plugin/include/

}
