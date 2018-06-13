package org.exampledriven.rabbitMQ;

import java.io.File;
import java.io.Serializable;

public class BigOpertaion implements Serializable {
    private String fileIds;
    private String accessToken;
    private String instanceURL;
    private boolean useSoap;
    private File fileToBeMerged;


    public String getFileIds() {
        return fileIds;
    }

    public void setFileIds(String fileIds) {
        this.fileIds = fileIds;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getInstanceURL() {
        return instanceURL;
    }

    public void setInstanceURL(String instanceURL) {
        this.instanceURL = instanceURL;
    }

    public boolean isUseSoap() {
        return useSoap;
    }

    public void setUseSoap(boolean useSoap) {
        this.useSoap = useSoap;
    }

    public File getFileToBeMerged() {
        return fileToBeMerged;
    }

    public void setFileToBeMerged(File fileToBeMerged) {
        this.fileToBeMerged = fileToBeMerged;
    }
}