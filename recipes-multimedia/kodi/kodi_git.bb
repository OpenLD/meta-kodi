SUMMARY = "Kodi Media Center"
DESCRIPTION = "Kodi is an award-winning free and open source home theater/media \ 
center software and entertainment hub for digital media. With its beautiful \
interface and powerful skinning engine, it's available for Android, BSD, Linux, \
macOS, iOS and Windows."

HOMEPAGE = "https://kodi.tv/"
BUGTRACKER = "https://github.com/xbmc/xbmc/issues"

require ${BPN}.inc
inherit cmake pkgconfig gettext python3-dir python3native

SRC_URI:append = " \
	file://0001-FindCrossGUID.cmake-fix-for-crossguid-0.2.2.patch \
	file://kodi-995.01-fix-missing-wayland-scanner-pkg-config.patch \
"

OECMAKE_FIND_ROOT_PATH_MODE_PROGRAM = "BOTH"

DEPENDS += " \
  autoconf-native \
  automake-native \
  curl-native \
  flatbuffers-native \
  googletest-native \
  gperf-native \
  kodi-tools-jsonschemabuilder-native \
  kodi-tools-texturepacker-native \
  nasm-native \
  swig-native \
  unzip-native \
  yasm-native \
  zip-native \
  \
  avahi \
  bzip2 \
  crossguid \
  curl \
  dcadec \
  faad2 \
  ffmpeg \
  flatbuffers \
  fmt \
  fontconfig \
  fribidi \
  fstrcmp \
  giflib \
  harfbuzz \
  libass \
  libcdio \
  libcec \
  libinput \
  libmicrohttpd \
  libnfs \
  libpcre \
  libplist \
  libssh \
  libtinyxml \
  libxkbcommon \
  libxml2 \
  libxslt \
  lzo \
  mpeg2dec \
  python3 \
  rapidjson \
  spdlog \
  sqlite3 \
  taglib \
  virtual/egl \
  wavpack \
  zlib \
"

# breaks compilation
CCACHE_DISABLE = "1"
ASNEEDED = ""

KODIVAAPIDEPENDS = "libva"
KODIVAAPIDEPENDS:append:x86 = " intel-vaapi-driver"
KODIVAAPIDEPENDS:append:x86-64 = " intel-vaapi-driver"

PACKAGECONFIG ?= " \
  ${@bb.utils.contains('VAAPISUPPORT', '1', 'vaapi', '', d)} \
  ${@bb.utils.contains('VDPAUSUPPORT', '1', 'vdpau', '', d)} \
  ${@bb.utils.filter('DISTRO_FEATURES', 'bluetooth lirc pulseaudio samba systemd', d)} \
  ${@bb.utils.filter('KODIGRAPHICALBACKEND', 'gbm wayland x11', d)} \
  airtunes \
  joystick \
  lcms \
"

# Core windowing system choices

PACKAGECONFIG[gbm] = "-DCORE_PLATFORM_NAME=gbm -DGBM_RENDER_SYSTEM=gles,,"
PACKAGECONFIG[wayland] = "-DCORE_PLATFORM_NAME=wayland -DWAYLAND_RENDER_SYSTEM=gles,,wayland wayland-native waylandpp waylandpp-native"
PACKAGECONFIG[x11] = "-DCORE_PLATFORM_NAME=x11,,libxinerama libxmu libxrandr libxtst glew"

# Features

PACKAGECONFIG[airtunes] = "-DENABLE_AIRTUNES=ON,-DENABLE_AIRTUNES=OFF"
PACKAGECONFIG[bluetooth] = ",,bluez5"
PACKAGECONFIG[dvdcss] = "-DENABLE_DVDCSS=ON,-DENABLE_DVDCSS=OFF"
PACKAGECONFIG[joystick] = ",,,kodi-addon-peripheral-joystick"
PACKAGECONFIG[lcms] = ",,lcms"
PACKAGECONFIG[lirc] = ",,lirc"
PACKAGECONFIG[mysql] = "-DENABLE_MYSQLCLIENT=ON,-DENABLE_MYSQLCLIENT=OFF,mysql5"
PACKAGECONFIG[optical] = "-DENABLE_OPTICAL=ON,-DENABLE_OPTICAL=OFF"
PACKAGECONFIG[pulseaudio] = "-DENABLE_PULSEAUDIO=ON,-DENABLE_PULSEAUDIO=OFF,pulseaudio"
PACKAGECONFIG[samba] = ",,samba"
PACKAGECONFIG[systemd] = ",,,kodi-systemd-service"
PACKAGECONFIG[vaapi] = "-DENABLE_VAAPI=ON,-DENABLE_VAAPI=OFF,${KODIVAAPIDEPENDS},${KODIVAAPIDEPENDS}"
PACKAGECONFIG[vdpau] = "-DENABLE_VDPAU=ON,-DENABLE_VDPAU=OFF,libvdpau,mesa-vdpau-drivers"

# Compilation tunes

PACKAGECONFIG[gold] = "-DENABLE_LDGOLD=ON,-DENABLE_LDGOLD=OFF"
PACKAGECONFIG[lto] = "-DUSE_LTO=${@oe.utils.cpu_count()},-DUSE_LTO=OFF"
PACKAGECONFIG[testing] = "-DENABLE_TESTING=ON,-DENABLE_TESTING=0FF,googletest"

# MIPS

LDFLAGS += "${TOOLCHAIN_OPTIONS}"
LDFLAGS:append:mipsarch = " -latomic -lpthread"
EXTRA_OECMAKE:append:mipsarch = " -DWITH_ARCH=${TARGET_ARCH}"

#| cmake/scripts/common/Platform.cmake:11 (message):
#|   You need to decide whether you want to use GL- or GLES-based rendering.
#|   Please set APP_RENDER_SYSTEM to either "gl" or "gles".
#|   For embedded systems you will usually want to use "gles".

KODI_OPENGL_STANDARD ?= "gles"

EXTRA_OECMAKE = " \
    -DAPP_RENDER_SYSTEM=${KODI_OPENGL_STANDARD} \
    \
    -DENABLE_INTERNAL_CROSSGUID=OFF \
    \
    -DNATIVEPREFIX=${STAGING_DIR_NATIVE}${prefix} \
    -DJava_JAVA_EXECUTABLE=/usr/bin/java \
    -DWITH_TEXTUREPACKER=${STAGING_BINDIR_NATIVE}/TexturePacker \
    -DWITH_JSONSCHEMABUILDER=${STAGING_BINDIR_NATIVE}/JsonSchemaBuilder \
    \
    -DENABLE_STATIC_LIBS=FALSE \
    -DCMAKE_NM='${NM}' \
    \
    -DFFMPEG_PATH=${STAGING_DIR_TARGET} \
    -DLIBDVD_INCLUDE_DIRS=${STAGING_INCDIR} \
    -DNFS_INCLUDE_DIR=${STAGING_INCDIR} \
    -DSHAIRPLAY_INCLUDE_DIR=${STAGING_INCDIR} \
    -DWAYLANDPP_PROTOCOLS_DIR=${STAGING_DATADIR}/waylandpp/protocols \
    -DWAYLANDPP_SCANNER=${STAGING_BINDIR_NATIVE}/wayland-scanner++ \
    -DCMAKE_BUILD_TYPE=RelWithDebInfo \
"

# for python modules
export HOST_SYS
export BUILD_SYS
export STAGING_LIBDIR
export STAGING_INCDIR
export PYTHON_DIR

export TARGET_PREFIX

do_configure:prepend() {
	# Ensure 'nm' can find the lto plugins 
	liblto=$(find ${STAGING_DIR_NATIVE} -name "liblto_plugin.so.0.0.0")
	mkdir -p ${STAGING_LIBDIR_NATIVE}/bfd-plugins
	ln -sf $liblto ${STAGING_LIBDIR_NATIVE}/bfd-plugins/liblto_plugin.so

	sed -i -e 's:CMAKE_NM}:}${TARGET_PREFIX}gcc-nm:' ${S}/xbmc/cores/DllLoader/exports/CMakeLists.txt
}

INSANE_SKIP:${PN} = "rpaths"

FILES:${PN} += "${datadir}/metainfo ${datadir}/xsessions ${datadir}/icons ${libdir}/xbmc ${datadir}/xbmc ${libdir}/firewalld"
FILES:${PN}-dbg += "${libdir}/kodi/.debug ${libdir}/kodi/*/.debug ${libdir}/kodi/*/*/.debug ${libdir}/kodi/*/*/*/.debug"

# kodi uses some kind of dlopen() method for libcec so we need to add it manually
# OpenGL builds need glxinfo, that's in mesa-demos
RRECOMMENDS:${PN}:append = " \
  libcec \
  libcurl \
  libnfs \
  ${@bb.utils.contains('PACKAGECONFIG', 'x11', 'xdyinfo xrandr xinit mesa-demos', '', d)} \
  python3 \
  python3-compression \
  python3-ctypes \
  python3-difflib \
  python3-html \
  python3-json \
  python3-netclient \
  python3-regex \
  python3-shell \
  python3-sqlite3 \
  python3-xmlrpc \
  tzdata-africa \
  tzdata-americas \
  tzdata-antarctica \
  tzdata-arctic \
  tzdata-asia \
  tzdata-atlantic \
  tzdata-australia \
  tzdata-europe \
  tzdata-pacific \
  xkeyboard-config \
"

RRECOMMENDS:${PN}:append:libc-glibc = " \
  glibc-charmap-ibm850 \
  glibc-gconv-ibm850 \
  glibc-charmap-ibm437 \
  glibc-gconv-ibm437 \
  glibc-gconv-unicode \
  glibc-gconv-utf-32 \
  glibc-charmap-utf-8 \
  glibc-localedata-en-us \
"

do_compile[network] = "1"
