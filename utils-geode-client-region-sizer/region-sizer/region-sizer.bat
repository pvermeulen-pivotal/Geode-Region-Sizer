set HERE=%cd%

set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_77
set CLASSPATH=%HERE%\conf;%HERE%\lib\*

"%JAVA_HOME%\bin\java" -cp %CLASSPATH% com.humana.base.gemfire.client.region.sizer.ClientRegionSizer %*

