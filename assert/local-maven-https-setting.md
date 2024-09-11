### 本地maven仓库配置

#### 1. 安装证书

`${JAVA_HOME}/keytool -import -alias nexus -file ./cert/192.168.130.243.cer  -keystore  ${JAVA_HOME}\lib\security\cacerts`

#### 2. maven配置

`mv ./maven-setting.xml ${MAVEN_HOME}/conf/setting.xml`





