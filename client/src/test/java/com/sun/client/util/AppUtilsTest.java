package com.sun.client.util;

import com.google.protobuf.ByteString;
import com.sun.client.model.Log;
import org.hyperledger.fabric.protos.peer.FabricProposalResponse;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.util.ResourceUtils;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FabricProposalResponse.ProposalResponse.class, FabricProposalResponse.Response.class, AppUtils.class})
public class AppUtilsTest {

  @Test
  public void testCreateAdminPeerEnrollment() throws Exception {
    File file = mock(File.class);
    File privateKeyFile = ResourceUtils.getFile("classpath:admin_private_key");
    File certificateFile = ResourceUtils.getFile("classpath:admin_certificate.pem");
    PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(file);
    when(file.listFiles())
            .thenReturn(new File[]{privateKeyFile})
            .thenReturn(new File[]{certificateFile});
    // Converting certificate file to string
    String certificate = new String(Files.readAllBytes(certificateFile.toPath()));
    // Converting private key file to PrivateKey
    String privateKeyAsString = new String(Files.readAllBytes(privateKeyFile.toPath()));
    privateKeyAsString = privateKeyAsString.replace("-----BEGIN PRIVATE KEY-----", "");
    privateKeyAsString = privateKeyAsString.replace("-----END PRIVATE KEY-----", "");
    BASE64Decoder b64 = new BASE64Decoder();
    byte[] decoded = b64.decodeBuffer(privateKeyAsString);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
    KeyFactory kf = KeyFactory.getInstance("EC");
    PrivateKey privateKey = kf.generatePrivate(spec);
    // Calling method under test
    Enrollment enrollment = AppUtils.createAdminPeerEnrollment();
    assertEquals(certificate, enrollment.getCert());
    assertEquals(privateKey, enrollment.getKey());

  }

  @Test
  public void testConvertToLog() throws Exception {
    ProposalResponse response = mock(ProposalResponse.class, RETURNS_DEEP_STUBS);
    FabricProposalResponse.ProposalResponse proposalResponse = PowerMockito.mock(FabricProposalResponse.ProposalResponse.class);
    FabricProposalResponse.Response response1 = PowerMockito.mock(FabricProposalResponse.Response.class);
    ByteString byteString = PowerMockito.mock(ByteString.class);
    Collection<ProposalResponse> responses = new LinkedList<>();
    // Sample test response in the format of original response
    String responsePayload = "{\"timestamp\":\"2019-01-07 08:01:19\",\"application\":\"test-application\",\"event\":\"test-event\",\"message\":\"test-message\"}";
    responses.add(response);
    // Mocking method chain call
    when(response.getProposalResponse()).thenReturn(proposalResponse);
    when(proposalResponse.getResponse()).thenReturn(response1);
    when(response1.getPayload()).thenReturn(byteString);
    when(byteString.size()).thenReturn(1);
    when(byteString.toStringUtf8()).thenReturn(responsePayload);
    // Calling method under test
    List<Log> logs = AppUtils.convertToLog(responses, false);
    assertEquals("test-application", logs.get(0).getApplication());
    assertEquals("test-event", logs.get(0).getEvent());
    assertEquals("test-message", logs.get(0).getMessage());
  }

}