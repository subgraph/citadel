require citadel-kernel.inc

SRC_URI[kernel.md5sum] = "60e1a229fb5ffc2a82462ed9400ce3d0"
SRC_URI[kernel.sha256sum] = "24ba70877549a3cf25dc5f12efd260d3e957bce64c087de98baf8968ee514895"

PATCH_DATE_TAG = "20171124142753"

SRC_URI[patch.md5sum] = "3c1521b8ada079fbbca372d5191fa351"
SRC_URI[patch.sha256sum] = "f25398621854074f1a286d97fd39224462c054b568c7ae8181765340a439faf9"

# Hard-coding SOUCE_DATE_EPOCH so that the kernel_do_compile function succeeds 
# with the BUILD_REPRODUCIBLE_BINARIES flag enabled.
# The formula for create this value is turning the PATCH_DATE_TAG timestamp
# into an epoch, ex: https://www.epochconverter.com/
export SOURCE_DATE_EPOCH="1511533673"
