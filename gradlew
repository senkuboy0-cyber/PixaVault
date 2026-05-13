#!/bin/sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
fi

# Check if gradle-wrapper.jar exists, if not download it
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading gradle-wrapper.jar..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    
    # Try to download from Gradle GitHub
    curl -sL -o "$WRAPPER_JAR" \
        "https://raw.githubusercontent.com/gradle/gradle/v9.1.0/gradle/wrapper/gradle-wrapper.jar" \
        --create-dirs
    
    # Fallback to another source if failed
    if [ ! -s "$WRAPPER_JAR" ]; then
        curl -sL -o "$WRAPPER_JAR" \
            "https://github.com/nicoulaj/gradle-wrapper-artifacts/raw/master/gradle-9.1.0/gradle-wrapper.jar" \
            --create-dirs
    fi
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

# For Cygwin or MSYS, switch paths to Windows format before running java
if [ "$cygwin" = "true" ] || [ "$msys" = "true" ] ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
    JAVACMD=`cygpath --unix "$JAVACMD"`
fi

# Check if wrapper JAR exists
if [ ! -f "$CLASSPATH" ]; then
    echo "Error: gradle-wrapper.jar not found at $CLASSPATH"
    echo "Please ensure the Gradle wrapper is properly installed."
    exit 1
fi

# Execute Gradle
exec "$JAVACMD" -Xmx64m -Xms64m \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
