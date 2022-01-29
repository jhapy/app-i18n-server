package org.jhapy.i18n.endpoint;

import com.github.javafaker.Faker;
import org.jhapy.dto.domain.i18n.MessageDTO;
import org.jhapy.dto.domain.i18n.MessageTrlDTO;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.*;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.messageTrl.GetMessageTrlQuery;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.i18n.commons.AbstractGlobalAxonServerTest;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MessageServiceEndpointIT extends AbstractGlobalAxonServerTest {
  private static final String TEST_USER_ID = "clavaud@me.com";

  private final String dataName = Faker.instance().aviation().airport();
  private final String dataDesc = Faker.instance().aviation().METAR();
  private final String valueName = Faker.instance().lorem().word();

  private final String language1 = Faker.instance().country().countryCode3();
  private final String language2 = Faker.instance().country().countryCode3();
  private final String language3 = Faker.instance().country().countryCode3();
  private final String language4 = Faker.instance().country().countryCode3();

  @Autowired private JacksonTester<ServiceResult<MessageDTO>> serviceResultForMessageDTOTester;

  @Autowired
  private JacksonTester<ServiceResult<MessageTrlDTO>> serviceResultForMessageTrlDTOTester;

  @Autowired
  private JacksonTester<ServiceResult<List<MessageTrlDTO>>> serviceResultForMessageTrlDTOListTester;

  @Autowired
  private JacksonTester<ServiceResult<List<MessageDTO>>> serviceResultForMessageDTOListTester;

  @Autowired
  private JacksonTester<ServiceResult<PageDTO<MessageDTO>>> serviceResultForMessageDTOPageTester;

  @Autowired private JacksonTester<GetMessageTrlQuery> getMessageTrlQueryTester;

  @BeforeAll
  public static void setUp() {}

  @AfterAll
  public static void cleanUp() {}

  @Test
  @DisplayName("Test Save an Message without Translation (MessageTrl)")
  void testSaveWithNoTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveWithNoTranslation");
    String testSuffix = "-testSaveWithNoTranslation";

    // given
    MessageDTO newMessage = new MessageDTO();
    newMessage.setName(dataName + testSuffix);
    newMessage.setCategory(dataDesc + testSuffix);

    SaveQuery<MessageDTO> saveQuery = new SaveQuery<>();
    saveQuery.setEntity(newMessage);

    String queryContent = saveQueryTester.write(saveQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/save")
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
    ServiceResult<MessageDTO> messageDTOServiceResult =
        serviceResultForMessageDTOTester.parse(resultContentString).getObject();
    assertTrue(messageDTOServiceResult.getIsSuccess());
    assertEquals(newMessage.getName(), messageDTOServiceResult.getData().getName());
    assertEquals(newMessage.getCategory(), messageDTOServiceResult.getData().getCategory());

    debug(loggerPrefix, "Result : " + messageDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test Save an Message with Translation (MessageTrl)")
  void testSaveWithTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveWithTranslation");
    String testSuffix = "-testSaveWithTranslation";

    // given
    MessageDTO newMessage = new MessageDTO();
    newMessage.setName(dataName + testSuffix);
    newMessage.setCategory(dataDesc + testSuffix);

    MessageTrlDTO messageTrlDTO = new MessageTrlDTO();
    messageTrlDTO.setValue(valueName + testSuffix);
    messageTrlDTO.setIso3Language(language1);
    newMessage.getTranslations().add(messageTrlDTO);

    SaveQuery<MessageDTO> saveQuery = new SaveQuery<>();
    saveQuery.setEntity(newMessage);

    String queryContent = saveQueryTester.write(saveQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/save")
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
    ServiceResult<MessageDTO> messageDTOServiceResult =
        serviceResultForMessageDTOTester.parse(resultContentString).getObject();
    assertTrue(messageDTOServiceResult.getIsSuccess());
    assertEquals(newMessage.getName(), messageDTOServiceResult.getData().getName());
    assertEquals(newMessage.getCategory(), messageDTOServiceResult.getData().getCategory());
    assertNotNull(newMessage.getTranslations());
    assertEquals(1, messageDTOServiceResult.getData().getTranslations().size());
    MessageTrlDTO resultMessageTrlDTO = messageDTOServiceResult.getData().getTranslations().get(0);
    assertEquals(newMessage.getName(), resultMessageTrlDTO.getName());
    assertEquals(messageTrlDTO.getValue(), resultMessageTrlDTO.getValue());

    debug(loggerPrefix, "Result : " + messageDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test Save several Messages without Translation (MessageTrl)")
  void testSaveAllWithNoTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveAllWithNoTranslation");
    String testSuffix = "-testSaveAllWithNoTranslation";

    // given
    MessageDTO newMessage1 = new MessageDTO();
    newMessage1.setName(dataName + testSuffix);
    newMessage1.setCategory(dataDesc + testSuffix);

    MessageDTO newMessage2 = new MessageDTO();
    newMessage2.setName(dataName + "-1" + testSuffix);
    newMessage2.setCategory(dataDesc + "-1" + testSuffix);

    SaveAllQuery<MessageDTO> saveAllQuery = new SaveAllQuery<>();
    saveAllQuery.setEntity(Arrays.asList(newMessage1, newMessage2));

    String queryContent = saveAllQueryTester.write(saveAllQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/saveAll")
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
    ServiceResult<List<MessageDTO>> messageDTOListServiceResult =
        serviceResultForMessageDTOListTester.parse(resultContentString).getObject();
    assertTrue(messageDTOListServiceResult.getIsSuccess());
    assertEquals(2, messageDTOListServiceResult.getData().size());

    debug(loggerPrefix, "Result : " + messageDTOListServiceResult.getData());
  }

  @Test
  @DisplayName("Test Save several Messages without Translation (MessageTrl)")
  void testSaveAllWithTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveAllWithTranslation");
    String testSuffix = "-testSaveAllWithTranslation";

    // given
    MessageDTO newMessage1 = new MessageDTO();
    newMessage1.setName(dataName + testSuffix);
    newMessage1.setCategory(dataDesc + testSuffix);

    MessageTrlDTO messageTrlDTO1 = new MessageTrlDTO();
    messageTrlDTO1.setValue(valueName + testSuffix);
    messageTrlDTO1.setIso3Language(language1);
    newMessage1.getTranslations().add(messageTrlDTO1);

    MessageDTO newMessage2 = new MessageDTO();
    newMessage2.setName(dataName + "-1" + testSuffix);
    newMessage2.setCategory(dataDesc + "-1" + testSuffix);

    MessageTrlDTO messageTrlDTO2 = new MessageTrlDTO();
    messageTrlDTO2.setValue(valueName + testSuffix);
    messageTrlDTO2.setIso3Language(language1);
    newMessage2.getTranslations().add(messageTrlDTO2);

    SaveAllQuery<MessageDTO> saveAllQuery = new SaveAllQuery<>();
    saveAllQuery.setEntity(Arrays.asList(newMessage1, newMessage2));

    String queryContent = saveAllQueryTester.write(saveAllQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/saveAll")
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
    ServiceResult<List<MessageDTO>> messageDTOListServiceResult =
        serviceResultForMessageDTOListTester.parse(resultContentString).getObject();
    assertTrue(messageDTOListServiceResult.getIsSuccess());
    assertEquals(2, messageDTOListServiceResult.getData().size());
    assertEquals(1, messageDTOListServiceResult.getData().get(0).getTranslations().size());
    assertEquals(1, messageDTOListServiceResult.getData().get(1).getTranslations().size());
    debug(loggerPrefix, "Result : " + messageDTOListServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetByName")
  void testGetByName() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetByName");
    String testSuffix = "-testGetByName";

    // given
    String name = dataName + testSuffix;
    createDummyMessageDTO(name, dataDesc + testSuffix);
    GetByNameQuery getByNameQuery = new GetByNameQuery();
    getByNameQuery.setName(name);

    String queryContent = getByNameQueryTester.write(getByNameQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/getByName")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_READ")))
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
    ServiceResult<MessageDTO> messageDTOServiceResult =
        serviceResultForMessageDTOTester.parse(resultContentString).getObject();
    assertTrue(messageDTOServiceResult.getIsSuccess());
    assertNotNull(messageDTOServiceResult.getData());
    assertEquals(name, messageDTOServiceResult.getData().getName());

    debug(loggerPrefix, "Result : " + messageDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetById")
  void testGetById() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetById");
    String testSuffix = "-testGetById";

    // given
    String name = dataName + testSuffix;
    UUID createdMessageId = createDummyMessageDTO(name, dataDesc + testSuffix);
    GetByIdQuery getByIdQuery = new GetByIdQuery();
    getByIdQuery.setId(createdMessageId);

    String queryContent = getByIdQueryTester.write(getByIdQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/getById")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_READ")))
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
    ServiceResult<MessageDTO> messageDTOServiceResult =
        serviceResultForMessageDTOTester.parse(resultContentString).getObject();
    assertTrue(messageDTOServiceResult.getIsSuccess());
    assertNotNull(messageDTOServiceResult.getData());
    assertEquals(name, messageDTOServiceResult.getData().getName());

    debug(loggerPrefix, "Result : " + messageDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetMessageTrls")
  void testGetMessageTrls() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetMessageTrls");
    String testSuffix = "-testGetMessageTrls";

    // given
    String name = dataName + testSuffix;
    UUID createdMessageId =
        createDummyMessageDTOWithTranslations(
            name, dataDesc + testSuffix, valueName + testSuffix, language1, language2);
    GetMessageTrlQuery getMessageTrlQuery = new GetMessageTrlQuery();
    getMessageTrlQuery.setMessageId(createdMessageId);

    String queryContent = getMessageTrlQueryTester.write(getMessageTrlQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/getMessageTrls")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_READ")))
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
    ServiceResult<List<MessageTrlDTO>> messageDTOServiceResult =
        serviceResultForMessageTrlDTOListTester.parse(resultContentString).getObject();
    assertTrue(messageDTOServiceResult.getIsSuccess());
    assertNotNull(messageDTOServiceResult.getData());

    assertEquals(2, messageDTOServiceResult.getData().size());

    debug(loggerPrefix, "Result : " + messageDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test FindByIso3")
  void testFindByIso3() throws Exception {
    String loggerPrefix = getLoggerPrefix("testFindByIso3");
    String testSuffix = "-testFindByIso3";

    // given
    String name = dataName + testSuffix;
    createDummyMessageDTOWithTranslations(
        name, dataDesc + testSuffix, valueName + testSuffix, language1, language3);
    FindByIso3Query findByIso3Query = new FindByIso3Query();
    findByIso3Query.setIso3Language(language3);

    String queryContent = findByIso3QueryTester.write(findByIso3Query).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/findByIso3")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_READ")))
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
    ServiceResult<List<MessageTrlDTO>> messageDTOServiceResult =
        serviceResultForMessageTrlDTOListTester.parse(resultContentString).getObject();
    assertTrue(messageDTOServiceResult.getIsSuccess());
    assertNotNull(messageDTOServiceResult.getData());

    assertEquals(1, messageDTOServiceResult.getData().size());
    assertEquals(name, messageDTOServiceResult.getData().get(0).getName());

    debug(loggerPrefix, "Result : " + messageDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetMessageTrlByNameAndIso3")
  void testGetMessageTrlByNameAndIso3() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetMessageTrlByNameAndIso3");
    String testSuffix = "-testGetMessageTrlByNameAndIso3";

    // given
    String name = dataName + testSuffix;
    createDummyMessageDTOWithTranslations(
        name, dataDesc + testSuffix, valueName + testSuffix, language1, language4);
    GetByNameAndIso3Query getByNameAndIso3Query = new GetByNameAndIso3Query();
    getByNameAndIso3Query.setName(name);
    getByNameAndIso3Query.setIso3Language(language4);

    String queryContent = getByNameAndIso3QueryTester.write(getByNameAndIso3Query).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/getMessageTrlByNameAndIso3")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_READ")))
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
    ServiceResult<MessageTrlDTO> messageDTOServiceResult =
        serviceResultForMessageTrlDTOTester.parse(resultContentString).getObject();
    assertTrue(messageDTOServiceResult.getIsSuccess());
    assertNotNull(messageDTOServiceResult.getData());

    assertEquals(name, messageDTOServiceResult.getData().getName());

    debug(loggerPrefix, "Result : " + messageDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetMessageTrlByNameAndIso3 with an unknown language")
  void testGetMessageTrlByNameAndIso3WithUnknowLanguage() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetMessageTrlByNameAndIso3");
    String testSuffix = "-testGetMessageTrlByNameAndIso3";

    // given
    String name = dataName + testSuffix;

    GetByNameAndIso3Query getByNameAndIso3Query = new GetByNameAndIso3Query();
    getByNameAndIso3Query.setName(name);
    getByNameAndIso3Query.setIso3Language(language1);

    String queryContent = getByNameAndIso3QueryTester.write(getByNameAndIso3Query).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/getMessageTrlByNameAndIso3")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_READ")))
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
    ServiceResult<MessageTrlDTO> messageDTOServiceResult =
        serviceResultForMessageTrlDTOTester.parse(resultContentString).getObject();
    assertTrue(messageDTOServiceResult.getIsSuccess());
    assertNotNull(messageDTOServiceResult.getData());

    assertEquals(name, messageDTOServiceResult.getData().getName());
    assertEquals(name, messageDTOServiceResult.getData().getValue());

    debug(loggerPrefix, "Result : " + messageDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test FindAnyMatching (search name)")
  void testFindAnyMatchingSearchByName() throws Exception {
    String loggerPrefix = getLoggerPrefix("testFindAnyMatchingSearchByName");
    String testSuffix = "-testFindAnyMatchingSearchByName";

    // given
    String name = dataName + testSuffix;
    createDummyMessageDTOWithTranslations(
        name, dataDesc + testSuffix, valueName + testSuffix, language1, language2);
    FindAnyMatchingQuery findAnyMatchingQuery = new FindAnyMatchingQuery();
    findAnyMatchingQuery.setFilter(name);

    String queryContent = findAnyMatchingQueryTester.write(findAnyMatchingQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/findAnyMatching")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_READ")))
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
    ServiceResult<PageDTO<MessageDTO>> messagePageServiceResult =
        serviceResultForMessageDTOPageTester.parse(resultContentString).getObject();
    assertTrue(messagePageServiceResult.getIsSuccess());
    assertNotNull(messagePageServiceResult.getData());

    assertEquals(1, messagePageServiceResult.getData().getTotalElements());
    assertEquals(name, messagePageServiceResult.getData().getContent().get(0).getName());

    debug(loggerPrefix, "Result : " + messagePageServiceResult.getData());
  }

  @Test
  @DisplayName("Test FindAnyMatching (search value)")
  void testFindAnyMatchingSearchByValue() throws Exception {
    String loggerPrefix = getLoggerPrefix("testFindAnyMatchingSearchByValue");
    String testSuffix = "-testFindAnyMatchingSearchByValue";

    // given
    String name = dataName + testSuffix;
    createDummyMessageDTOWithTranslations(
        name, dataDesc + testSuffix, valueName + testSuffix, language1, language2);
    FindAnyMatchingQuery findAnyMatchingQuery = new FindAnyMatchingQuery();
    findAnyMatchingQuery.setFilter(valueName + testSuffix);

    String queryContent = findAnyMatchingQueryTester.write(findAnyMatchingQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/findAnyMatching")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_READ")))
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
    ServiceResult<PageDTO<MessageDTO>> messagePageServiceResult =
        serviceResultForMessageDTOPageTester.parse(resultContentString).getObject();
    assertTrue(messagePageServiceResult.getIsSuccess());
    assertNotNull(messagePageServiceResult.getData());

    assertEquals(1, messagePageServiceResult.getData().getTotalElements());
    assertEquals(name, messagePageServiceResult.getData().getContent().get(0).getName());

    debug(loggerPrefix, "Result : " + messagePageServiceResult.getData());
  }

  @Test
  @DisplayName("Test CountAnyMatching")
  void testCountAnyMatching() throws Exception {
    String loggerPrefix = getLoggerPrefix("testCountAnyMatching");
    String testSuffix = "-testCountAnyMatching";

    // given
    String name = dataName + testSuffix;
    createDummyMessageDTOWithTranslations(
        name, dataDesc + testSuffix, valueName + testSuffix, language1, language2);
    CountAnyMatchingQuery countAnyMatchingQuery = new CountAnyMatchingQuery();
    countAnyMatchingQuery.setFilter(name);

    String queryContent = countAnyMatchingQueryTester.write(countAnyMatchingQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/countAnyMatching")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_READ")))
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
    ServiceResult<Long> messageDTOServiceResult =
        serviceResultForLongTester.parse(resultContentString).getObject();
    assertTrue(messageDTOServiceResult.getIsSuccess());
    assertNotNull(messageDTOServiceResult.getData());

    assertEquals(1, messageDTOServiceResult.getData());

    debug(loggerPrefix, "Result : " + messageDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetAll")
  void testGetAll() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetAll");
    String testSuffix = "-testGetAll";

    // given
    String name = dataName + testSuffix;
    createDummyMessageDTO(name, dataDesc + testSuffix);
    BaseRemoteQuery baseRemoteQuery = new BaseRemoteQuery();

    String queryContent = baseRemoteQueryTester.write(baseRemoteQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/getAll")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_I18N_READ")))
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
    ServiceResult<List<MessageDTO>> messageDTOListServiceResult =
        serviceResultForMessageDTOListTester.parse(resultContentString).getObject();
    assertTrue(messageDTOListServiceResult.getIsSuccess());
    assertNotNull(messageDTOListServiceResult.getData());
    assertEquals(messageRepository.count(), messageDTOListServiceResult.getData().size());

    debug(loggerPrefix, "Result : " + messageDTOListServiceResult.getData());
  }

  @Test
  @DisplayName("Test Delete (with no translation)")
  void testDeleteWithNoTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testDeleteWithNoTranslation");
    String testSuffix = "-testDeleteWithNoTranslation";

    // given
    MessageDTO newMessage = new MessageDTO();
    newMessage.setName(dataName + testSuffix);
    newMessage.setCategory(dataDesc + testSuffix);

    UUID messageId = postNewCreateMessage(newMessage);

    DeleteByIdQuery deleteByIdQuery = new DeleteByIdQuery();
    deleteByIdQuery.setId(messageId);

    String queryContent = deleteByIdQueryTester.write(deleteByIdQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/delete")
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
    ServiceResult<Void> voidServiceResult =
        serviceResultForVoidTester.parse(resultContentString).getObject();
    assertTrue(voidServiceResult.getIsSuccess());
    assertNull(voidServiceResult.getData());

    assertThat(elementRepository.findById(messageId)).isEmpty();

    debug(loggerPrefix, "Result : " + voidServiceResult.getData());
  }

  @Test
  @DisplayName("Test Delete (with translations)")
  void testDeleteWithTranslations() throws Exception {
    String loggerPrefix = getLoggerPrefix("testDeleteWithTranslations");
    String testSuffix = "-testDeleteWithTranslations";

    // given
    MessageDTO newMessage = new MessageDTO();
    newMessage.setName(dataName + testSuffix);
    newMessage.setCategory(dataDesc + testSuffix);

    MessageTrlDTO messageTrlDTO = new MessageTrlDTO();
    messageTrlDTO.setValue(valueName + testSuffix);
    messageTrlDTO.setIso3Language(language1);
    newMessage.getTranslations().add(messageTrlDTO);

    UUID messageId = postNewCreateMessage(newMessage);

    DeleteByIdQuery deleteByIdQuery = new DeleteByIdQuery();
    deleteByIdQuery.setId(messageId);

    String queryContent = deleteByIdQueryTester.write(deleteByIdQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/delete")
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
    ServiceResult<Void> voidServiceResult =
        serviceResultForVoidTester.parse(resultContentString).getObject();
    assertTrue(voidServiceResult.getIsSuccess());
    assertNull(voidServiceResult.getData());

    assertThat(messageRepository.findById(messageId)).isEmpty();
    assertEquals(0, messageTrlRepository.findByParentId(messageId).size());

    debug(loggerPrefix, "Result : " + voidServiceResult.getData());
  }

  protected UUID createDummyMessageDTO(String name, String category) {
    Message message = new Message();
    message.setName(name);
    message.setCategory(category);

    return messageRepository.save(message).getId();
  }

  protected UUID createDummyMessageDTOWithTranslations(
      String name, String category, String value, String iso1, String iso2) {
    Message message = new Message();
    message.setName(name);
    message.setCategory(category);

    MessageTrl messageTrl1 = new MessageTrl();
    messageTrl1.setValue(value + "-1");
    messageTrl1.setIso3Language(iso1);

    MessageTrl messageTrl2 = new MessageTrl();
    messageTrl2.setValue(value + "-2");
    messageTrl2.setIso3Language(iso2);

    message.getTranslations().put(messageTrl1.getIso3Language(), messageTrl1);
    message.getTranslations().put(messageTrl2.getIso3Language(), messageTrl2);

    return messageRepository.save(message).getId();
  }

  private UUID postNewCreateMessage(MessageDTO message) throws Exception {
    String loggerPrefix = getLoggerPrefix("postNewCreateMessage");
    String testSuffix = "-postNewCreateMessage";

    // given
    SaveQuery<MessageDTO> saveQuery = new SaveQuery<>();
    saveQuery.setEntity(message);

    String queryContent = saveQueryTester.write(saveQuery).getJson();

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/messageService/save")
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
    ServiceResult<MessageDTO> messageDTOServiceResult =
        serviceResultForMessageDTOTester.parse(resultContentString).getObject();
    assertTrue(messageDTOServiceResult.getIsSuccess());
    assertEquals(message.getName(), messageDTOServiceResult.getData().getName());
    assertEquals(message.getCategory(), messageDTOServiceResult.getData().getCategory());

    return messageDTOServiceResult.getData().getId();
  }
}