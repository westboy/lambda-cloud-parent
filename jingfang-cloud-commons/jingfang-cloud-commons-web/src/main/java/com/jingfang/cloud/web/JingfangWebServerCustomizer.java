package com.jingfang.cloud.web;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.SessionIdGenerator;
import org.apache.catalina.core.StandardContext;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

import java.beans.PropertyChangeListener;

/**
 * @author Jin
 */
public class JingfangWebServerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {


    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addContextCustomizers(context -> context.setManager(new M1Manager()));
    }

    @SuppressWarnings({"squid:S1186"})
    private static class M1Manager implements Manager {
        private static final Session[] SESSIONS = new Session[0];
        private static final Context CONTEXT = new StandardContext();

        @Override
        public Context getContext() {
            return CONTEXT;
        }

        @Override
        public void setContext(Context context) {
        }

        @Override
        public SessionIdGenerator getSessionIdGenerator() {
            return null;
        }

        @Override
        public void setSessionIdGenerator(SessionIdGenerator sessionIdGenerator) {

        }

        @Override
        public long getSessionCounter() {
            return 0;
        }

        @Override
        public void setSessionCounter(long sessionCounter) {

        }

        @Override
        public int getMaxActive() {
            return 0;
        }

        @Override
        public void setMaxActive(int maxActive) {

        }

        @Override
        public int getActiveSessions() {
            return 0;
        }

        @Override
        public long getExpiredSessions() {
            return 0;
        }

        @Override
        public void setExpiredSessions(long expiredSessions) {

        }

        @Override
        public int getRejectedSessions() {
            return 0;
        }

        @Override
        public int getSessionMaxAliveTime() {
            return 0;
        }

        @Override
        public void setSessionMaxAliveTime(int sessionMaxAliveTime) {

        }

        @Override
        public int getSessionAverageAliveTime() {
            return 0;
        }

        @Override
        public int getSessionCreateRate() {
            return 0;
        }

        @Override
        public int getSessionExpireRate() {
            return 0;
        }

        @Override
        public void add(Session session) {

        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {

        }

        @Override
        @SuppressWarnings("all")
        public void changeSessionId(Session session) {

        }

        @Override
        public String rotateSessionId(Session session) {
            return null;
        }

        @Override
        public void changeSessionId(Session session, String newId) {

        }

        @Override
        public Session createEmptySession() {
            return null;
        }

        @Override
        public Session createSession(String sessionId) {
            return null;
        }

        @Override
        public Session findSession(String id) {
            return null;
        }

        @Override
        public Session[] findSessions() {
            return SESSIONS;
        }

        @Override
        public void load() {

        }

        @Override
        public void remove(Session session) {

        }

        @Override
        public void remove(Session session, boolean update) {

        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {

        }

        @Override
        public void unload() {

        }

        @Override
        public void backgroundProcess() {

        }

        @Override
        public boolean willAttributeDistribute(String name, Object value) {
            return false;
        }

        @Override
        public void setNotifyBindingListenerOnUnchangedValue(boolean notifyBindingListenerOnUnchangedValue) {

        }

        @Override
        public void setNotifyAttributeListenerOnUnchangedValue(boolean notifyAttributeListenerOnUnchangedValue) {

        }
    }
}
