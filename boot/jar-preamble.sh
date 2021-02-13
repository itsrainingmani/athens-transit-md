{{!
This file contains a jar preamble script which will be
prepended to the resulting uber-jar and make it possible
to execute the jar as a normal system executable (without
java -jar) on both windows and *nix systems. 

The script below uses a special syntax which intermixes both sh
and cmd code. It is written this way because it is used in
system shell-outs directly in otherwise portable code. See
https://stackoverflow.com/questions/17510688 for details.

This block is a moustache templating comment and will not be
included in the resulting jar preamble.

Please see https://github.com/BrunoBonacci/lein-binplus
for details on what templating expressions etc are possible for
the script below.

The script, as it stands, supports both direct java and drip jvm
accelerator invocations. In other words, when you just run your
custom command

$> bob

it will use the below script to check if 'drip' is available
on the path and if so, use it to launch the process. This only
works on osx and linux as I have not had the time to figure
out how to do this on windows.

Note that when using drip, the java executable that should be 
used can be set via the DRIP_JAVA_CMD environment variable as 
in: 

~> DRIP_JAVA_CMD=/lib/java-14/jre/bin/java

When not using drip and calling java directly the java to use 
can be set by: 

  * setting the JAVA_CMD environment variable to point at a 
    java executable 
  * setting JAVA_HOME to point to a directory which has either 
    a bin/java (jre) or a jre/bin/java (jdk). 

-Matias 2018.01.11
}}
:; if [ -z "$JAVA_CMD" -a -n "$JAVA_HOME" -a -x "$JAVA_HOME/bin/java" ];     then JAVA_CMD="$JAVA_HOME/bin/java"; fi
:; if [ -z "$JAVA_CMD" -a -n "$JAVA_HOME" -a -x "$JAVA_HOME/jre/bin/java" ]; then JAVA_CMD="$JAVA_HOME/jre/bin/java"; fi 
:; if [ -z "$JAVA_CMD" ]; then JAVA_CMD="java"; fi
:; hash drip  >/dev/null 2>&1 # make sure the command call on the next line has an up to date worldview
:;if command -v drip >/dev/null 2>&1 ; then CMD=drip; else CMD="${JAVA_CMD}"; fi
:;exec $CMD -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:-OmitStackTraceInFastThrow -client -Xbootclasspath/a:"$0" athens-transit-md.core "$@"; exit $?
@echo off
java -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:-OmitStackTraceInFastThrow -client -Xbootclasspath/a:%1 athens-transit-md.core "%~f0" %*
goto :eof
