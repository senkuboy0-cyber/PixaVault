#!/bin/sh

##############################################################################
##
##  Gradle start up script for UN*X
##  Auto-downloads gradle-wrapper.jar if not present
##
##############################################################################

# Gradle version to use
GRADLE_VERSION=9.1.0
GRADLE_DISTRIBUTION_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

# Attempt to set APP_HOME
APP_HOME="$(cd "$(dirname "$0")" && pwd -P)"

# Create gradle/wrapper directory if not exists
mkdir -p "${APP_HOME}/gradle/wrapper"

# Download gradle-wrapper.jar if not present
if [ ! -f "${APP_HOME}/gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "⬇️ Downloading gradle-wrapper.jar..."
    
    # Try official Gradle repo first
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}/gradle/wrapper/gradle-wrapper.jar" \
        -o "${APP_HOME}/gradle/wrapper/gradle-wrapper.jar"
    
    # Fallback to GitHub releases
    if [ ! -s "${APP_HOME}/gradle/wrapper/gradle-wrapper.jar" ]; then
        curl -sL "https://github.com/gradle/gradle/releases/download/v${GRADLE_VERSION}/gradle-${GRADLE_VERSION}-wrapper.jar" \
            -o "${APP_HOME}/gradle/wrapper/gradle-wrapper.jar"
    fi
    
    echo "✅ gradle-wrapper.jar downloaded successfully!"
fi

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
fi

# Gradle wrapper classpath
CLASSPATH="${APP_HOME}/gradle/wrapper/gradle-wrapper.jar"

# Add default JVM options
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# OS specific support
case "$(uname)" in
    CYGWIN* ) cygwin=true ;;
    Darwin* ) darwin=true ;;
    MINGW* ) msys=true ;;
esac

# For Cygwin, switch paths
if $cygwin ; then
    APP_HOME="$(cygpath --path --mixed "$APP_HOME")"
    CLASSPATH="$(cygpath --path --mixed "$CLASSPATH")"
fi

# Execute Gradle
exec "$JAVACMD" $DEFAULT_JVM_OPTS \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
