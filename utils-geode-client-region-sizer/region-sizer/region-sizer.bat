set HERE=%cd%

set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_77
set CLASSPATH=%HERE%\conf;%HERE%\lib\*

"%JAVA_HOME%\bin\java" -cp %CLASSPATH% pivotal.geode.client.region.sizer.ClientRegionSizer %*

