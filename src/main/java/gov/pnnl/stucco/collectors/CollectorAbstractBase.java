package gov.pnnl.stucco.collectors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import gov.pnnl.stucco.doc_service_client.*;

/**
 * $OPEN_SOURCE_DISCLAIMER$
 */

/** Abstract base class used in implementing Collectors. */
public abstract class CollectorAbstractBase implements Collector {
    
    protected final ContentConverter m_contentConverter = new ContentConverter();
    protected final Map<String, String> m_metadata = new HashMap<String, String>();
    protected final QueueSender m_queueSender           = new QueueSender();
    
    /** raw content from source */
    protected byte[] m_rawContent;
    
    /** time the data was collected */
    protected Date m_timestamp = null;
    
    private int numberOfThreads = 1;
    
    /** Map of configuration data for the specific collector. */
    private Map<String, String> configData;

    protected CollectorAbstractBase(Map<String, String> configData) {
        this.configData = configData;
        
        // default metadata comes from configuration
        m_metadata.put("contentType", configData.get("content-type"));
        m_metadata.put("sourceName", configData.get("source-name"));
        m_metadata.put("sourceUrl", configData.get("source-URI"));
        
        Map<String, Object> configMap = (Map<String, Object>) Config.getMap();
        Map<String, Object> stuccoMap = (Map<String, Object>) configMap.get("stucco");
        Map<String, Object> docServiceConfig = (Map<String, Object>) stuccoMap.get("document-service");
       
        try {
            DocServiceClient docServiceClient = new DocServiceClient(docServiceConfig);
        
            // we create a delegate for queueSender
            m_queueSender.setDocService(docServiceClient);
        } catch (DocServiceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send the content when requested
     */
    public void send() {
        m_queueSender.send(m_metadata, m_rawContent);
    }
    
    public void setNumberOfThreads(int threadCount) {
        
        // we're only allow the number of threads to be between 1 and 8 (at this time)
        if (threadCount > 0 && threadCount < 9) {
            numberOfThreads = threadCount;
        }
    }

    public abstract void collect();
    
    public abstract void clean();
}
