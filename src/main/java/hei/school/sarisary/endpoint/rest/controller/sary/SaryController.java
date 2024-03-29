package hei.school.sarisary.endpoint.rest.controller.sary;

import static hei.school.sarisary.file.FileHashAlgorithm.NONE;

import hei.school.sarisary.PojaGenerated;
import hei.school.sarisary.file.BucketComponent;
import hei.school.sarisary.file.FileHash;
import hei.school.sarisary.service.sary.SaryService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@PojaGenerated
public class SaryController {

  BucketComponent bucketComponent;
  SaryService service;

  private static final String HEALTH_KEY = "photo/";

  @PutMapping(
      value = "/black-and-white/{Id}",
      consumes = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
  public ResponseEntity<Void> addNewPhoto(@PathVariable String Id, @RequestBody byte[] image)
      throws Exception {
    byte[] GrayImage;
    var suffix = ".png";
    var file2BucketKey = Id + "-gray" + suffix;

    GrayImage = service.toGRay(image);

    File imageToUpload = new File(Id + suffix);
    FileUtils.writeByteArrayToFile(imageToUpload, image);

    File imageGrayToUpload = new File(file2BucketKey);
    FileUtils.writeByteArrayToFile(imageGrayToUpload, GrayImage);

    can_upload_file(imageToUpload, Id + suffix);
    can_upload_file(imageGrayToUpload, file2BucketKey);

    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping("/black-and-white/{id}")
  public Map<String, String> getPhotoById(@PathVariable String id) {
    String suffix = ".png";
    Map<String, String> response = new HashMap<>();
    response.put("original_url", String.valueOf(can_presign(id + suffix).toString()));
    response.put("transformed_url", String.valueOf(can_presign(id + "-gray" + suffix).toString()));

    return response;
  }

  private File can_upload_file_then_download_file(File toUpload, String bucketKey)
      throws IOException {
    bucketComponent.upload(toUpload, bucketKey);

    var downloaded = bucketComponent.download(bucketKey);
    var downloadedContent = Files.readString(downloaded.toPath());
    var uploadedContent = Files.readString(toUpload.toPath());
    if (!uploadedContent.equals(downloadedContent)) {
      throw new RuntimeException("Uploaded and downloaded contents mismatch");
    }

    return downloaded;
  }

  private void can_upload_file(File toUpload, String bucketKey) throws IOException {
    bucketComponent.upload(toUpload, bucketKey);
  }

  private File download_file(String bucketKey) {
    return bucketComponent.download(bucketKey);
  }

  private FileHash can_upload_directory(File toUpload, String bucketKey) {
    var hash = bucketComponent.upload(toUpload, bucketKey);
    if (!NONE.equals(hash.algorithm())) {
      throw new RuntimeException("FileHashAlgorithm.NONE expected but got: " + hash.algorithm());
    }
    return hash;
  }

  private URL can_presign(String fileBucketKey) {
    return bucketComponent.presign(fileBucketKey, Duration.ofMinutes(2));
  }
}
