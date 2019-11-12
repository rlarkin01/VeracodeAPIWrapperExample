package hello.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String location = "upload-dir";
    private String resultsLocation = "results-dir";

    public String getLocation() {
        return location;
    }

    public String getResultsLocation() {
        return resultsLocation;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
