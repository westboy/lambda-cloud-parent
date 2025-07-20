package com.lambda.cloud.actuator.resolver;

import com.lambda.cloud.core.Constants;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

/**
 * @author jin
 */
@Slf4j
public class PathResourceResolver extends PathMatchingResourcePatternResolver implements InfoContributor {

    private final String locationPattern;

    public PathResourceResolver(String locationPattern) {
        this.locationPattern = locationPattern;
    }

    public Map<String, Object> initialization() {
        Map<String, Object> outcomes = new HashMap<>(16);
        try {
            Resource[] resources = getResources(locationPattern);
            Map<String, Object> back = new HashMap<>(16);
            Map<String, Object> packages = new HashMap<>(16);
            for (Resource resource : resources) {
                URL url = resource.getURL();
                if (ResourceUtils.isJarURL(url)) {
                    URLConnection connection = url.openConnection();
                    if (connection instanceof JarURLConnection jarCon) {
                        ResourceUtils.useCachesIfNecessary(jarCon);
                        JarFile jarFile = jarCon.getJarFile();
                        Manifest manifest = jarFile.getManifest();
                        final Attributes attributes = manifest.getMainAttributes();
                        String title = attributes.getValue("Implementation-Title");
                        ResourceIndicator indicator = new ResourceIndicator();
                        indicator.setVersion(attributes.getValue("Implementation-Version"));
                        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_PATTERN);
                        String dateString = dateFormat.format(new Date(resource.lastModified()));
                        indicator.setModified(dateString);
                        packages.put(title, indicator);
                    }
                }
            }
            back.put("packages", packages);
            outcomes.put("back", back);
        } catch (Exception e) {
            log.error("PathResourceResolver initialization has exception. msg: {}", e.getMessage());
        }
        return outcomes;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> parameters = this.initialization();
        builder.withDetails(parameters);
    }
}
