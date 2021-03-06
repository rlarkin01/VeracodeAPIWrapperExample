package hello.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    Path store(MultipartFile file);

    Stream<Path> loadAll();

    Stream<Path> loadResults();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

}
