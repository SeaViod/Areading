#!/bin/sh
# Minimal gradlew - downloads Gradle if needed
# Based on Gradle wrapper

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# Darwin/MinGW/Cygwin specific
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

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
fi

WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="gradle/wrapper/gradle-wrapper.properties"

# If wrapper jar doesn't exist, download it
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading gradle-wrapper.jar..."
    GRADLE_WRAPPER_URL="https://services.gradle.org/distributions/gradle-8.2-wrapper.jar.sha256"
    # Use a known minimal wrapper approach
    mkdir -p "$(dirname "$WRAPPER_JAR")"
    # Download from a mirror
    if command -v curl > /dev/null 2>&1; then
        curl -sL "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar" -o "$WRAPPER_JAR" || \
        curl -sL "https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/8.2/gradle-wrapper-8.2.jar" -o "$WRAPPER_JAR"
    elif command -v wget > /dev/null 2>&1; then
        wget -q "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar" -O "$WRAPPER_JAR" || \
        wget -q "https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/8.2/gradle-wrapper-8.2.jar" -O "$WRAPPER_JAR"
    fi
fi

# Run gradle
exec "$JAVACMD" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
