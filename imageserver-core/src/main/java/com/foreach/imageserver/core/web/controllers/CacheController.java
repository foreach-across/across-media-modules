package com.foreach.imageserver.core.web.controllers;

import org.springframework.stereotype.Controller;

@Controller
public class CacheController
{
/*
    @Autowired
    private CacheManager cacheManager;

    @Value("${accessToken}")
    private String accessToken;

    @RequestMapping("/cacheStats")
    @ResponseBody
    public void cacheStats(HttpServletResponse response, @RequestParam(value = "token", required = false) String accessToken) {
        if (!this.accessToken.equals(accessToken)) {
            respond("Access denied.", response);
            return;
        }

        StringBuilder output = new StringBuilder();
        String[] cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);

            int nrOfEntries = cache.getSize();
            long inMemorySize = cache.calculateInMemorySize();

            StatisticsGateway statistics = cache.getStatistics();
            long hitCount = statistics.cacheHitCount();
            long missCount = statistics.cacheMissCount();

            output.append(String.format("CACHE %s:\n", cacheName));
            output.append(String.format("\tNumber of entries:\t\t\t%d\n", nrOfEntries));
            output.append(String.format("\tIn-Memory Size (bytes):\t\t\t%d\n", inMemorySize));
            output.append(String.format("\tHit count:\t\t\t\t%d\n", hitCount));
            output.append(String.format("\tMiss count:\t\t\t\t%d\n", missCount));
            output.append("\n");
        }
        respond(output.toString(), response);
    }

    @RequestMapping("/cacheKeys")
    @ResponseBody
    public void cacheKeys(HttpServletResponse response, @RequestParam(value = "cacheName") String cacheName, @RequestParam(value = "token", required = false) String accessToken) {
        if (!this.accessToken.equals(accessToken)) {
            respond("Access denied.", response);
            return;
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            List<String> cacheKeys = cache.getKeys();
            if (cacheKeys.isEmpty()) {
                respond("The cache is empty.", response);
            } else {
                StringBuilder output = new StringBuilder();
                for (String cacheKey : cacheKeys) {
                    output.append(cacheKey).append("\n");
                }
                respond(output.toString(), response);
            }
        } else {
            respond("No such cache.", response);
        }
    }

    @RequestMapping("/flushAllCaches")
    @ResponseBody
    public void flushAllCaches(HttpServletResponse response, @RequestParam(value = "token", required = false) String accessToken) {
        if (!this.accessToken.equals(accessToken)) {
            respond("Access denied.", response);
            return;
        }

        String[] cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            cacheManager.getCache(cacheName).removeAll();
        }
        respond("All caches were flushed.", response);
    }

    @RequestMapping("/flushCache")
    @ResponseBody
    public void flushCache(HttpServletResponse response, @RequestParam(value = "cacheName") String cacheName, @RequestParam(value = "token", required = false) String accessToken) {
        if (!this.accessToken.equals(accessToken)) {
            respond("Access denied.", response);
            return;
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.removeAll();
            respond("Cache was flushed.", response);
        } else {
            respond("No such cache.", response);
        }
    }

    private void respond(String text, HttpServletResponse response) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("text/plain");

        OutputStream responseStream = null;
        try {
            responseStream = response.getOutputStream();
            responseStream.write(text.getBytes());
        } catch (IOException ioe) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } finally {
            IOUtils.closeQuietly(responseStream);
        }
    }
*/
}

