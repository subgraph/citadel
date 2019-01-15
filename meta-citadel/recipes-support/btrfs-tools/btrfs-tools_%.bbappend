# Put python library into a separate package and avoid dragging in python as RDEPENDS
PACKAGES =+ "${PN}-python"
FILES_${PN}-python = "${libdir}/python${PYTHON_BASEVERSION}"
RDEPENDS_${PN}_remove = "${PYTHON_PN}-core"
