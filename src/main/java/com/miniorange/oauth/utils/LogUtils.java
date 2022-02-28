package com.miniorange.oauth.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils implements Log {

    private Class logClass = null;

    private LogUtils(Class s) {
        this.logClass = s;
    }

    public static LogUtils getLog(Class s) {
        return new LogUtils(s);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isFatalEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void trace(Object o) {
        this.showMessage(o);
    }

    @Override
    public void trace(Object o, Throwable throwable) {
        this.showMessage(o);
    }

    @Override
    public void debug(Object o) {
        this.showMessage(o);
    }

    @Override
    public void debug(Object o, Throwable throwable) {
        this.showMessage(o);
    }

    @Override
    public void info(Object o) {
        this.showMessage(o);
    }

    @Override
    public void info(Object o, Throwable throwable) {
        this.showMessage(o);
    }

    @Override
    public void warn(Object o) {
        this.showMessage(o);
    }

    @Override
    public void warn(Object o, Throwable throwable) {
        this.showMessage(o);
    }

    @Override
    public void error(Object o) {
        this.showMessage(o);
    }

    @Override
    public void error(Object o, Throwable throwable) {
        this.showMessage(o);
    }

    @Override
    public void fatal(Object o) {
        this.showMessage(o);
    }

    @Override
    public void fatal(Object o, Throwable throwable) {
        this.showMessage(o);
    }

    private void showMessage(Object o){
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String fileContext = "[ MO CUSTOM MESSAGE " +  timeStamp + " : " + logClass.getName() + "] : ";
    };
}
