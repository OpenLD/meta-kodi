SUMMARY = "NFS client library"
HOMEPAGE = "https://github.com/sahlberg/libnfs"
BUGTRACKER = "https://github.com/sahlberg/libnfs"

LICENSE = "LGPL-2.1-only & GPL-3.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=825301ba17efc9d188ee0abd4b924ada"

SRC_URI = "git://github.com/sahlberg/libnfs.git;protocol=https;branch=master"
SRCREV = "${PN}-${PV}"
S = "${WORKDIR}/git"

inherit cmake
