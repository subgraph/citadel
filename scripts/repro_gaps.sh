#!/bin/bash

DATE=`date '+%Y-%m-%d %H:%M:%S'`

# Path to the .debs to deploy within the build dir of the Citadel root
DEPLOY_PATH="build/tmp-glibc/deploy/deb/"

TEST_PATH=$(realpath $1)
CONTROL_PATH=$(realpath $2)

usage () {
    echo ""
    echo "Usage:"
    echo "${0} <path_to_test_build> <path_to_control_build> [--output filename]"
    echo ""
    echo "Compare the reproducibility gaps between two Citadel builds"
    echo ""
    echo "Options:"
    echo "    --output filename    Output the list of unreproducible packages (optional)"
    echo ""
    echo "Example:"
    echo ""
    echo "${0} src/citadel src/citadel-control --output gaps.txt"
    echo ""
}

# Find all .debs, will ignore Packages.gz and other files
find_all() {
    local all_debs=$(find -L ${TEST_PATH}/${DEPLOY_PATH} -name '*.deb' -exec readlink -f {} \;)
    echo $all_debs
}

# Main

if [ -z "$TEST_PATH" ]; then 
    echo "Missing argument for the test build path"
    usage
    exit 1
fi

if [ -z "$CONTROL_PATH" ]; then
    echo "Missing argument for the control build path"
    usage
    exit 1
fi

if [ ! -d "$TEST_PATH" ]; then
    echo "Test path argument is not a directory"
    usage
    exit 1
fi

if [ ! -d "$CONTROL_PATH" ]; then
    echo "Control path argument is not a directory"
    usage
    exit 1
fi

if [ "$TEST_PATH" = "$CONTROL_PATH" ]; then
    echo "Test path and control path cannot be the same"
    usage
    exit 1
fi

shift 2

OUTPUT=false

while (( $# > 1 )); do case $1 in
    --output) OUTPUT=$2;;
    *) break;
esac; shift 2
done

echo "Enumerating files..."

ALL_DEBS=$(find_all)
NUM_FILES=( $ALL_DEBS )

SAME=0
DIFF=0
TOTAL=0

echo ""
echo "Comparing reproducibility gaps for ${#NUM_FILES[@]} .debs - ${DATE}"
if [ ! "$OUTPUT" = false ]; then
    echo "Logging unreproducible packages to ${OUTPUT}"
    echo "Unreproducible packages - ${DATE}" > ${OUTPUT}
fi
echo "Test Path: ${TEST_PATH}/${DEPLOY_PATH}"
echo "Control Path: ${CONTROL_PATH}/${DEPLOY_PATH}"
echo "Same: ${SAME}"
echo "Different: ${DIFF}"
echo "Total: ${TOTAL}"
tput civis

clean_up () {
    tput cnorm
    tput sgr0
    exit 2
}

trap clean_up SIGHUP SIGINT SIGTERM

for file1 in $ALL_DEBS; do
    TOTAL=$((TOTAL + 1))
    tput sc
    tput cuu 1
    echo "Total: ${TOTAL}"
    tput rc
    file2=${file1/$TEST_PATH/$CONTROL_PATH}
    if [ -f ${file2} ]; then
        cmp ${file1} ${file2} -s
        if [ $? -eq 0 ]; then
            SAME=$((SAME + 1))
            tput sc
            tput cuu 3
            echo "Same: $(tput setaf 2) ${SAME} $(tput sgr0)"
            tput rc
        else 
            DIFF=$((DIFF + 1)) 
            if [ ! "$OUTPUT" = false ]; then
                echo ${file1} >> ${OUTPUT}
            fi
            tput sc
            tput cuu 2
            echo "Different: $(tput setaf 1) ${DIFF} $(tput sgr0)"
            tput rc
        fi
    fi
done

tput cnorm
tput sgr0


