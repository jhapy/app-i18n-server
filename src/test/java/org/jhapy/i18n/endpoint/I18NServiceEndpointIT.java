package org.jhapy.i18n.endpoint;

import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.util.FileUtil;
import org.jhapy.cqrs.command.i18n.CreateActionCommand;
import org.jhapy.cqrs.command.i18n.CreateElementCommand;
import org.jhapy.cqrs.command.i18n.CreateMessageCommand;
import org.jhapy.dto.domain.i18n.*;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.ImportI18NFileQuery;
import org.jhapy.dto.serviceQuery.i18n.QueryFileUploadStatusQuery;
import org.jhapy.dto.serviceQuery.i18n.ResetEventsQuery;
import org.jhapy.dto.serviceResponse.FileUploadStatusResponse;
import org.jhapy.i18n.commons.AbstractGlobalAxonServerTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
      "axon.eventhandling.processors.element-group.mode=tracking",
      "axon.eventhandling.processors.action-group.mode=tracking",
      "axon.eventhandling.processors.message-group.mode=tracking",
      /*"spring.jpa.show-sql=true",
      "logging.level.org.hibernate.SQL=debug",
      "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"*/ })
public class I18NServiceEndpointIT extends AbstractGlobalAxonServerTest {
  private static final String TEST_USER_ID = "clavaud@me.com";

  @Autowired private JacksonTester<ImportI18NFileQuery> importI18NFileQueryTester;
  @Autowired private JacksonTester<ResetEventsQuery> resetEventsQueryTester;
  @Autowired private JacksonTester<QueryFileUploadStatusQuery> queryFileUploadStatusQueryTester;

  @Autowired
  private JacksonTester<ServiceResult<FileUploadStatusResponse>>
      serviceResultForFileUploadStatusResponseTester;

  @BeforeAll
  public static void setUp() {}

  @AfterAll
  public static void cleanUp() {}

  @Test
  @DisplayName("Test Import and Excel file")
  public void testImportExcelFile() throws Exception {
    String loggerPrefix = getLoggerPrefix("testImportExcelFile");

    // given
    File excelFile = new File(getClass().getClassLoader().getResource("i18n.xlsx").getFile());
    ImportI18NFileQuery importI18NFileQuery = new ImportI18NFileQuery();
    importI18NFileQuery.setFilename(excelFile.getName());
    importI18NFileQuery.setFileContent(ArrayUtils.toObject(FileUtil.readAsByteArray(excelFile)));

    String queryContent = importI18NFileQueryTester.write(importI18NFileQuery).getJson();
    // debug(loggerPrefix,"Json Input = {0}", queryContent );

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/i18NService/importI18NFile")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_WRITE")))
                    .with(user(TEST_USER_ID))
                    .with(csrf())
                    .content(queryContent)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    // then
    String resultContentString = result.getResponse().getContentAsString();
    debug(loggerPrefix, "Json Result = {0}", resultContentString);
    assertNotNull(resultContentString);
    ServiceResult<UUID> importI18NServiceResult =
        serviceResultForUUIDTester.parse(resultContentString).getObject();
    assertTrue(importI18NServiceResult.getIsSuccess());
    UUID fileUploadId = importI18NServiceResult.getData();
    await()
        .timeout(1, TimeUnit.MINUTES)
        .untilAsserted(
            () ->
                assertThat(lookupFileStatus(fileUploadId))
                    .hasFieldOrPropertyWithValue("isSuccess", true)
                    /*.satisfies(entity -> debug(loggerPrefix, "Entity: {0}", entity))*/
                    .extracting("data")
                    .hasFieldOrPropertyWithValue("uploadId", fileUploadId)
                    .hasFieldOrPropertyWithValue("isValidated", true)
                    .hasFieldOrPropertyWithValue("isImported", true));

    debug(loggerPrefix, "Result : " + importI18NServiceResult.getData());
  }

  @Test
  void resetElementGroupEvents() throws Exception {
    String loggerPrefix = getLoggerPrefix("resetElementGroupEvents");

    // given
    long countInit = elementRepository.count();
    long countLookupInit = elementLookupRepository.count();

    int createdElements = createStubElements();

    Thread.sleep(1000);

    long initialCount = createdElements + countInit;
    long initialLookupCount = createdElements + countLookupInit;

    await()
        .timeout(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              assertThat(elementRepository.count()).isEqualTo(initialCount);
              assertThat(elementLookupRepository.count()).isEqualTo(initialLookupCount);
            });

    ResetEventsQuery resetEventsQuery = new ResetEventsQuery("element-group");

    String queryContent = resetEventsQueryTester.write(resetEventsQuery).getJson();

    elementRepository.deleteAll();

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/i18NService/resetEvents")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_WRITE")))
                    .with(user(TEST_USER_ID))
                    .with(csrf())
                    .content(queryContent)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    // then
    String resultContentString = result.getResponse().getContentAsString();
    debug(loggerPrefix, "Json Result = {0}", resultContentString);
    assertNotNull(resultContentString);
    ServiceResult<String> resetEventsServiceResult =
        serviceResultForStringTester.parse(resultContentString).getObject();
    assertTrue(resetEventsServiceResult.getIsSuccess());

    await()
        .timeout(3, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              long count = elementRepository.count();
              debug(loggerPrefix, "Element repository contains {0} elements", count);
              debug(
                  loggerPrefix,
                  "Element Lookup repository contains {0} elements",
                  elementLookupRepository.count());
              assertThat(count).isEqualTo(initialCount);
              assertThat(elementLookupRepository.count()).isEqualTo(initialLookupCount);
            });
  }

  @Test
  void resetActionGroupEvents() throws Exception {
    String loggerPrefix = getLoggerPrefix("resetActionGroupEvents");

    // given
    long countInit = actionRepository.count();
    long countLookupInit = actionLookupRepository.count();

    int createdActions = createStubActions();

    Thread.sleep(1000);

    long initialCount = createdActions + countInit;
    long initialLookupCount = createdActions + countLookupInit;

    await()
        .timeout(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              assertThat(actionRepository.count()).isEqualTo(initialCount);
              assertThat(actionLookupRepository.count()).isEqualTo(initialLookupCount);
            });

    ResetEventsQuery resetEventsQuery = new ResetEventsQuery("action-group");

    String queryContent = resetEventsQueryTester.write(resetEventsQuery).getJson();

    actionRepository.deleteAll();

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/i18NService/resetEvents")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_WRITE")))
                    .with(user(TEST_USER_ID))
                    .with(csrf())
                    .content(queryContent)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    // then
    String resultContentString = result.getResponse().getContentAsString();
    debug(loggerPrefix, "Json Result = {0}", resultContentString);
    assertNotNull(resultContentString);
    ServiceResult<String> resetEventsServiceResult =
        serviceResultForStringTester.parse(resultContentString).getObject();
    assertTrue(resetEventsServiceResult.getIsSuccess());

    await()
        .timeout(3, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              long count = actionRepository.count();
              debug(loggerPrefix, "Action repository contains {0} actions", count);
              debug(
                  loggerPrefix,
                  "Action Lookup repository contains {0} actions",
                  actionLookupRepository.count());
              assertThat(count).isEqualTo(initialCount);
              assertThat(actionLookupRepository.count()).isEqualTo(initialLookupCount);
            });
  }

  @Test
  void resetMessageGroupEvents() throws Exception {
    String loggerPrefix = getLoggerPrefix("resetMessageGroupEvents");

    // given
    long countInit = messageRepository.count();
    long countLookupInit = messageLookupRepository.count();

    int createdMessages = createStubMessages();

    Thread.sleep(1000);

    long initialCount = createdMessages + countInit;
    long initialLookupCount = createdMessages + countLookupInit;

    await()
        .timeout(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              assertThat(messageRepository.count()).isEqualTo(initialCount);
              assertThat(messageLookupRepository.count()).isEqualTo(initialLookupCount);
            });

    ResetEventsQuery resetEventsQuery = new ResetEventsQuery("message-group");

    String queryContent = resetEventsQueryTester.write(resetEventsQuery).getJson();

    messageRepository.deleteAll();

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/i18NService/resetEvents")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_WRITE")))
                    .with(user(TEST_USER_ID))
                    .with(csrf())
                    .content(queryContent)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    // then
    String resultContentString = result.getResponse().getContentAsString();
    debug(loggerPrefix, "Json Result = {0}", resultContentString);
    assertNotNull(resultContentString);
    ServiceResult<String> resetEventsServiceResult =
        serviceResultForStringTester.parse(resultContentString).getObject();
    assertTrue(resetEventsServiceResult.getIsSuccess());

    await()
        .timeout(3, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              long count = messageRepository.count();
              debug(loggerPrefix, "Message repository contains {0} messages", count);
              debug(
                  loggerPrefix,
                  "Message Lookup repository contains {0} messages",
                  messageLookupRepository.count());
              assertThat(count).isEqualTo(initialCount);
              assertThat(messageLookupRepository.count()).isEqualTo(initialLookupCount);
            });
  }

  protected ServiceResult<FileUploadStatusResponse> lookupFileStatus(UUID fileUploadId)
      throws Exception {
    String loggerPrefix = getLoggerPrefix("lookupFileStatus");
    QueryFileUploadStatusQuery query = new QueryFileUploadStatusQuery();
    query.setUploadId(fileUploadId);

    String queryContent = queryFileUploadStatusQueryTester.write(query).getJson();

    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/i18NService/queryUploadStatus")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_WRITE")))
                    .with(user(TEST_USER_ID))
                    .with(csrf())
                    .content(queryContent)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    String resultContentString = result.getResponse().getContentAsString();
    debug(loggerPrefix, "Json Result = {0}", resultContentString);
    assertNotNull(resultContentString);
    return serviceResultForFileUploadStatusResponseTester.parse(resultContentString).getObject();
  }

  private int createStubElements() {
    String loggerPrefix = getLoggerPrefix("createStubElements");

    List<CompletableFuture<Object>> completableFutureList =
        IntStream.rangeClosed(1, 100)
            .mapToObj(this::createOneElement)
            .map(createElementCommand -> commandGateway.send(createElementCommand))
            .collect(Collectors.toList());
    List<Object> objectList =
        completableFutureList.stream().map(CompletableFuture::join).collect(Collectors.toList());

    debug(loggerPrefix, "Created {0} elements", objectList.size());

    return objectList.size();
  }

  private int createStubActions() {
    String loggerPrefix = getLoggerPrefix("createStubActions");

    List<CompletableFuture<Object>> completableFutureList =
        IntStream.rangeClosed(1, 100)
            .mapToObj(this::createOneAction)
            .map(createActionCommand -> commandGateway.send(createActionCommand))
            .collect(Collectors.toList());
    List<Object> objectList =
        completableFutureList.stream().map(CompletableFuture::join).collect(Collectors.toList());

    debug(loggerPrefix, "Created {0} actions", objectList.size());

    return objectList.size();
  }

  private int createStubMessages() {
    String loggerPrefix = getLoggerPrefix("createStubMessages");

    List<CompletableFuture<Object>> completableFutureList =
        IntStream.rangeClosed(1, 100)
            .mapToObj(this::createOneMessage)
            .map(createMessageCommand -> commandGateway.send(createMessageCommand))
            .collect(Collectors.toList());
    List<Object> objectList =
        completableFutureList.stream().map(CompletableFuture::join).collect(Collectors.toList());

    debug(loggerPrefix, "Created {0} messages", objectList.size());

    return objectList.size();
  }

  private CreateElementCommand createOneElement(int i) {
    ElementDTO entity = new ElementDTO();
    entity.setName("Test-" + i);
    entity.setCategory("Category X");

    ElementTrlDTO elementTrlEngDTO = new ElementTrlDTO();
    elementTrlEngDTO.setValue("Test-Value-" + i);
    elementTrlEngDTO.setIso3Language("eng");

    ElementTrlDTO elementTrlFreDTO = new ElementTrlDTO();
    elementTrlFreDTO.setValue("Test-Value-" + i);
    elementTrlFreDTO.setIso3Language("fre");

    entity.setTranslations(Arrays.asList(elementTrlEngDTO, elementTrlFreDTO));

    return new CreateElementCommand(entity);
  }

  private CreateActionCommand createOneAction(int i) {
    ActionDTO entity = new ActionDTO();
    entity.setName("Test-" + i);
    entity.setCategory("Category X");

    ActionTrlDTO actionTrlEngDTO = new ActionTrlDTO();
    actionTrlEngDTO.setValue("Test-Value-" + i);
    actionTrlEngDTO.setIso3Language("eng");

    ActionTrlDTO actionTrlFreDTO = new ActionTrlDTO();
    actionTrlFreDTO.setValue("Test-Value-" + i);
    actionTrlFreDTO.setIso3Language("fre");

    entity.setTranslations(Arrays.asList(actionTrlEngDTO, actionTrlFreDTO));

    return new CreateActionCommand(entity);
  }

  private CreateMessageCommand createOneMessage(int i) {
    MessageDTO entity = new MessageDTO();
    entity.setName("Test-" + i);
    entity.setCategory("Category X");

    MessageTrlDTO messageTrlEngDTO = new MessageTrlDTO();
    messageTrlEngDTO.setValue("Test-Value-" + i);
    messageTrlEngDTO.setIso3Language("eng");

    MessageTrlDTO messageTrlFreDTO = new MessageTrlDTO();
    messageTrlFreDTO.setValue("Test-Value-" + i);
    messageTrlFreDTO.setIso3Language("fre");

    entity.setTranslations(Arrays.asList(messageTrlEngDTO, messageTrlFreDTO));

    return new CreateMessageCommand(entity);
  }
}