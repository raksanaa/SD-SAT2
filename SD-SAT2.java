import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;


@SpringBootApplication
@RestController
@RequestMapping("/api")
public class PersonalizedNewsApplication {

    private final Map<Integer, List<String>> userTopics = new HashMap<>();

    private final List<Map<String, Object>> articles = new ArrayList<>();

    public PersonalizedNewsApplication() {
        addArticle(101, "Latest Technology News",
                "Technology", "New developments in technology.", 9200);
        addArticle(102, "Sports News Today",
                "Sports", "Latest updates from the world of sports.", 8500);
        addArticle(103, "Business Market Update",
                "Business", "Important business and market updates.", 7800);
        addArticle(104, "Artificial Intelligence News",
                "Technology", "Recent developments in artificial intelligence.", 9700);
        addArticle(105, "International News",
                "World", "Important international news and events.", 6900);
    }

    public static void main(String[] args) {
        SpringApplication.run(PersonalizedNewsApplication.class, args);
    }

    private void addArticle(int id, String title, String topic,
                            String description, int views) {
        Map<String, Object> article = new LinkedHashMap<>();
        article.put("id", id);
        article.put("title", title);
        article.put("topic", topic);
        article.put("description", description);
        article.put("views", views);
        articles.add(article);
    }

    // ROUND 2: Search news using a keyword.
    @GetMapping("/news/search")
    public Map<String, Object> searchNews(@RequestParam String q) {
        List<Map<String, Object>> results = new ArrayList<>();
        String keyword = q.toLowerCase(Locale.ROOT);

        for (Map<String, Object> article : articles) {
            String title = article.get("title").toString().toLowerCase(Locale.ROOT);
            String topic = article.get("topic").toString().toLowerCase(Locale.ROOT);
            String description = article.get("description").toString()
                    .toLowerCase(Locale.ROOT);

            if (title.contains(keyword) || topic.contains(keyword)
                    || description.contains(keyword)) {
                results.add(article);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("keyword", q);
        response.put("count", results.size());
        response.put("articles", results);
        return response;
    }

    // ROUND 2: Retrieve one article using its ID.
    @GetMapping("/articles/{id}")
    public Map<String, Object> getArticle(@PathVariable int id) {
        for (Map<String, Object> article : articles) {
            if ((int) article.get("id") == id) {
                return article;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Article not found");
    }

    // ROUND 2: Follow a topic for a user.
    @PostMapping("/users/{userId}/topics")
    public Map<String, Object> followTopic(
            @PathVariable int userId,
            @RequestBody Map<String, String> request) {

        String topic = request.get("topic");

        if (topic == null || topic.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Topic cannot be empty");
        }

        userTopics.putIfAbsent(userId, new ArrayList<>());
        List<String> topics = userTopics.get(userId);

        if (!topics.contains(topic)) {
            topics.add(topic);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Topic followed successfully");
        response.put("userId", userId);
        response.put("topic", topic);
        response.put("topics", topics);
        return response;
    }

    // Retrieve the topics followed by a user.
    @GetMapping("/users/{userId}/topics")
    public Map<String, Object> getTopics(@PathVariable int userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("topics",
                userTopics.getOrDefault(userId, Collections.emptyList()));
        return response;
    }

    // ROUND 2 / ROUND 3: Generate a basic personalized news feed.
    @GetMapping("/users/{userId}/feed")
    public Map<String, Object> personalizedFeed(@PathVariable int userId) {
        List<String> topics = userTopics.getOrDefault(
                userId, Collections.emptyList());
        List<Map<String, Object>> feed = new ArrayList<>();

        for (Map<String, Object> article : articles) {
            String topic = article.get("topic").toString();
            if (topics.contains(topic)) {
                feed.add(article);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("followedTopics", topics);
        response.put("articles", feed);
        return response;
    }

    // ROUND 2: Return trending articles sorted by view count.
    @GetMapping("/news/trending")
    public List<Map<String, Object>> trendingNews() {
        List<Map<String, Object>> trending = new ArrayList<>(articles);

        trending.sort((first, second) ->
                Integer.compare(
                        (int) second.get("views"),
                        (int) first.get("views")));

        return trending;
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "running");
        response.put("application", "Personalized News Platform");
        return response;
    }
}
