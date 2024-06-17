package uk.ac.york.idk503.performancetest.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public final class MetadataService {
    private static final Logger LOG = LoggerFactory.getLogger(MetadataService.class);
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    public Map<String, String> getBase64EncodedId(final int id) {
        final byte[] bytes = ByteBuffer.allocate(Integer.BYTES).putInt(id).array();
        return Map.of("Base64EncodedId", Base64.getEncoder().encodeToString(bytes));
    }

    public Map<String, String> getMemoryMetadata() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("Used Heap Memory MB", String.valueOf(memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024)));
        metadata.put("Used Non Heap Memory MB",String.valueOf(memoryBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024)));

        return metadata;
    }

    public Map<String, String> getThreadMetadata() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("Thread Count",String.valueOf(threadMXBean.getThreadCount()));
        metadata.put("Thread CPU Time",String.valueOf(threadMXBean.getCurrentThreadCpuTime()));
        metadata.put("Thread User Time",String.valueOf(threadMXBean.getCurrentThreadUserTime()));
        metadata.put("Daemon Thread Count",String.valueOf(threadMXBean.getDaemonThreadCount()));
        metadata.put("Peak Thread Count",String.valueOf(threadMXBean.getPeakThreadCount()));

        return metadata;
    }

    public boolean getRequest(final URI uri) throws InterruptedException {
        ResponseEntity<String> responseEntity;

        while(true) {
            try{
                responseEntity = new RestTemplate().getForEntity(uri, String.class);
                return responseEntity.getStatusCode().equals(HttpStatus.OK);
            } catch (RestClientException e){
                LOG.error("RestClientException - {}", e.getMessage());
                Thread.sleep(500);
            }
        }
    }
}
