package org.exampledriven.rabbitMQ;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class BigOpertaion implements Serializable {
    private String fileIds;
    private String accessToken;
    private String instanceURL;
    private boolean useSoap;
    private List<byte[]> listOfByteArrays;

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

    public List<byte[]> getListOfByteArrays() {
        return listOfByteArrays;
    }

    public void setListOfByteArrays(List<byte[]> listOfByteArrays) {
        this.listOfByteArrays = listOfByteArrays;
    }
}
