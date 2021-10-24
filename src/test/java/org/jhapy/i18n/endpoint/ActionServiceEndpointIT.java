package org.jhapy.i18n.endpoint;

import com.github.javafaker.Faker;
import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.dto.domain.i18n.ActionTrlDTO;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.*;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.actionTrl.GetActionTrlQuery;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.i18n.commons.AbstractGlobalAxonServerTest;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
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

public class ActionServiceEndpointIT extends AbstractGlobalAxonServerTest {
  private static final String TEST_USER_ID = "clavaud@me.com";

  private final String dataName = Faker.instance().aviation().airport();
  private final String dataDesc = Faker.instance().aviation().METAR();
  private final String valueName = Faker.instance().lorem().word();
  private final String tooltipName = Faker.instance().lorem().sentence();

  private final String language1 = Faker.instance().country().countryCode3();
  private final String language2 = Faker.instance().country().countryCode3();
  private final String language3 = Faker.instance().country().countryCode3();
  private final String language4 = Faker.instance().country().countryCode3();

  @Autowired private JacksonTester<ServiceResult<ActionDTO>> serviceResultForActionDTOTester;
  @Autowired private JacksonTester<ServiceResult<ActionTrlDTO>> serviceResultForActionTrlDTOTester;

  @Autowired
  private JacksonTester<ServiceResult<List<ActionTrlDTO>>> serviceResultForActionTrlDTOListTester;

  @Autowired
  private JacksonTester<ServiceResult<List<ActionDTO>>> serviceResultForActionDTOListTester;

  @Autowired
  private JacksonTester<ServiceResult<PageDTO<ActionDTO>>> serviceResultForActionDTOPageTester;

  @Autowired private JacksonTester<GetActionTrlQuery> getActionTrlQueryTester;

  @BeforeAll
  public static void setUp() {}

  @AfterAll
  public static void cleanUp() {}

  @Test
  @DisplayName("Test Save an Action without Translation (ActionTrl)")
  void testSaveWithNoTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveWithNoTranslation");
    String testSuffix = "-testSaveWithNoTranslation";

    // given
    ActionDTO newAction = new ActionDTO();
    newAction.setName(dataName + testSuffix);
    newAction.setCategory(dataDesc + testSuffix);

    SaveQuery<ActionDTO> saveQuery = new SaveQuery<>();
    saveQuery.setEntity(newAction);

    String queryContent = saveQueryTester.write(saveQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/save")
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
    ServiceResult<ActionDTO> actionDTOServiceResult =
        serviceResultForActionDTOTester.parse(resultContentString).getObject();
    assertTrue(actionDTOServiceResult.getIsSuccess());
    assertEquals(newAction.getName(), actionDTOServiceResult.getData().getName());
    assertEquals(newAction.getCategory(), actionDTOServiceResult.getData().getCategory());

    debug(loggerPrefix, "Result : " + actionDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test Save an Action with Translation (ActionTrl)")
  void testSaveWithTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveWithTranslation");
    String testSuffix = "-testSaveWithTranslation";

    // given
    ActionDTO newAction = new ActionDTO();
    newAction.setName(dataName + testSuffix);
    newAction.setCategory(dataDesc + testSuffix);

    ActionTrlDTO actionTrlDTO = new ActionTrlDTO();
    actionTrlDTO.setValue(valueName + testSuffix);
    actionTrlDTO.setTooltip(tooltipName + testSuffix);
    actionTrlDTO.setIso3Language(language1);
    newAction.getTranslations().add(actionTrlDTO);

    SaveQuery<ActionDTO> saveQuery = new SaveQuery<>();
    saveQuery.setEntity(newAction);

    String queryContent = saveQueryTester.write(saveQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/save")
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
    ServiceResult<ActionDTO> actionDTOServiceResult =
        serviceResultForActionDTOTester.parse(resultContentString).getObject();
    assertTrue(actionDTOServiceResult.getIsSuccess());
    assertEquals(newAction.getName(), actionDTOServiceResult.getData().getName());
    assertEquals(newAction.getCategory(), actionDTOServiceResult.getData().getCategory());
    assertNotNull(newAction.getTranslations());
    assertEquals(1, actionDTOServiceResult.getData().getTranslations().size());
    ActionTrlDTO resultActionTrlDTO = actionDTOServiceResult.getData().getTranslations().get(0);
    assertEquals(newAction.getName(), resultActionTrlDTO.getName());
    assertEquals(actionTrlDTO.getValue(), resultActionTrlDTO.getValue());
    assertEquals(actionTrlDTO.getTooltip(), resultActionTrlDTO.getTooltip());

    debug(loggerPrefix, "Result : " + actionDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test Save several Actions without Translation (ActionTrl)")
  void testSaveAllWithNoTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveAllWithNoTranslation");
    String testSuffix = "-testSaveAllWithNoTranslation";

    // given
    ActionDTO newAction1 = new ActionDTO();
    newAction1.setName(dataName + testSuffix);
    newAction1.setCategory(dataDesc + testSuffix);

    ActionDTO newAction2 = new ActionDTO();
    newAction2.setName(dataName + "-1" + testSuffix);
    newAction2.setCategory(dataDesc + "-1" + testSuffix);

    SaveAllQuery<ActionDTO> saveAllQuery = new SaveAllQuery<>();
    saveAllQuery.setEntity(Arrays.asList(newAction1, newAction2));

    String queryContent = saveAllQueryTester.write(saveAllQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/saveAll")
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
    ServiceResult<List<ActionDTO>> actionDTOListServiceResult =
        serviceResultForActionDTOListTester.parse(resultContentString).getObject();
    assertTrue(actionDTOListServiceResult.getIsSuccess());
    assertEquals(2, actionDTOListServiceResult.getData().size());

    debug(loggerPrefix, "Result : " + actionDTOListServiceResult.getData());
  }

  @Test
  @DisplayName("Test Save several Actions without Translation (ActionTrl)")
  void testSaveAllWithTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveAllWithTranslation");
    String testSuffix = "-testSaveAllWithTranslation";

    // given
    ActionDTO newAction1 = new ActionDTO();
    newAction1.setName(dataName + testSuffix);
    newAction1.setCategory(dataDesc + testSuffix);

    ActionTrlDTO actionTrlDTO1 = new ActionTrlDTO();
    actionTrlDTO1.setValue(valueName + testSuffix);
    actionTrlDTO1.setTooltip(tooltipName + testSuffix);
    actionTrlDTO1.setIso3Language(language1);
    newAction1.getTranslations().add(actionTrlDTO1);

    ActionDTO newAction2 = new ActionDTO();
    newAction2.setName(dataName + "-1" + testSuffix);
    newAction2.setCategory(dataDesc + "-1" + testSuffix);

    ActionTrlDTO actionTrlDTO2 = new ActionTrlDTO();
    actionTrlDTO2.setValue(valueName + testSuffix);
    actionTrlDTO2.setTooltip(tooltipName + testSuffix);
    actionTrlDTO2.setIso3Language(language1);
    newAction2.getTranslations().add(actionTrlDTO2);

    SaveAllQuery<ActionDTO> saveAllQuery = new SaveAllQuery<>();
    saveAllQuery.setEntity(Arrays.asList(newAction1, newAction2));

    String queryContent = saveAllQueryTester.write(saveAllQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/saveAll")
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
    ServiceResult<List<ActionDTO>> actionDTOListServiceResult =
        serviceResultForActionDTOListTester.parse(resultContentString).getObject();
    assertTrue(actionDTOListServiceResult.getIsSuccess());
    assertEquals(2, actionDTOListServiceResult.getData().size());
    assertEquals(1, actionDTOListServiceResult.getData().get(0).getTranslations().size());
    assertEquals(1, actionDTOListServiceResult.getData().get(1).getTranslations().size());
    debug(loggerPrefix, "Result : " + actionDTOListServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetByName")
  void testGetByName() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetByName");
    String testSuffix = "-testGetByName";

    // given
    String name = dataName + testSuffix;
    createDummyActionDTO(name, dataDesc + testSuffix);
    GetByNameQuery getByNameQuery = new GetByNameQuery();
    getByNameQuery.setName(name);

    String queryContent = getByNameQueryTester.write(getByNameQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/getByName")
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
    ServiceResult<ActionDTO> actionDTOServiceResult =
        serviceResultForActionDTOTester.parse(resultContentString).getObject();
    assertTrue(actionDTOServiceResult.getIsSuccess());
    assertNotNull(actionDTOServiceResult.getData());
    assertEquals(name, actionDTOServiceResult.getData().getName());

    debug(loggerPrefix, "Result : " + actionDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetById")
  void testGetById() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetById");
    String testSuffix = "-testGetById";

    // given
    String name = dataName + testSuffix;
    UUID createdActionId = createDummyActionDTO(name, dataDesc + testSuffix);
    GetByIdQuery getByIdQuery = new GetByIdQuery();
    getByIdQuery.setId(createdActionId);

    String queryContent = getByIdQueryTester.write(getByIdQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/getById")
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
    ServiceResult<ActionDTO> actionDTOServiceResult =
        serviceResultForActionDTOTester.parse(resultContentString).getObject();
    assertTrue(actionDTOServiceResult.getIsSuccess());
    assertNotNull(actionDTOServiceResult.getData());
    assertEquals(name, actionDTOServiceResult.getData().getName());

    debug(loggerPrefix, "Result : " + actionDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetActionTrls")
  void testGetActionTrls() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetActionTrls");
    String testSuffix = "-testGetActionTrls";

    // given
    String name = dataName + testSuffix;
    UUID createdActionId =
        createDummyActionDTOWithTranslations(
            name,
            dataDesc + testSuffix,
            valueName + testSuffix,
            tooltipName + testSuffix,
            language1,
            language2);
    GetActionTrlQuery getActionTrlQuery = new GetActionTrlQuery();
    getActionTrlQuery.setActionId(createdActionId);

    String queryContent = getActionTrlQueryTester.write(getActionTrlQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/getActionTrls")
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
    ServiceResult<List<ActionTrlDTO>> actionDTOServiceResult =
        serviceResultForActionTrlDTOListTester.parse(resultContentString).getObject();
    assertTrue(actionDTOServiceResult.getIsSuccess());
    assertNotNull(actionDTOServiceResult.getData());

    assertEquals(2, actionDTOServiceResult.getData().size());

    debug(loggerPrefix, "Result : " + actionDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test FindByIso3")
  void testFindByIso3() throws Exception {
    String loggerPrefix = getLoggerPrefix("testFindByIso3");
    String testSuffix = "-testFindByIso3";

    // given
    String name = dataName + testSuffix;
    createDummyActionDTOWithTranslations(
        name,
        dataDesc + testSuffix,
        valueName + testSuffix,
        tooltipName + testSuffix,
        language1,
        language3);
    FindByIso3Query findByIso3Query = new FindByIso3Query();
    findByIso3Query.setIso3Language(language3);

    String queryContent = findByIso3QueryTester.write(findByIso3Query).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/findByIso3")
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
    ServiceResult<List<ActionTrlDTO>> actionDTOServiceResult =
        serviceResultForActionTrlDTOListTester.parse(resultContentString).getObject();
    assertTrue(actionDTOServiceResult.getIsSuccess());
    assertNotNull(actionDTOServiceResult.getData());

    assertEquals(1, actionDTOServiceResult.getData().size());
    assertEquals(name, actionDTOServiceResult.getData().get(0).getName());

    debug(loggerPrefix, "Result : " + actionDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetActionTrlByNameAndIso3")
  void testGetActionTrlByNameAndIso3() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetActionTrlByNameAndIso3");
    String testSuffix = "-testGetActionTrlByNameAndIso3";

    // given
    String name = dataName + testSuffix;
    createDummyActionDTOWithTranslations(
        name,
        dataDesc + testSuffix,
        valueName + testSuffix,
        tooltipName + testSuffix,
        language1,
        language4);
    GetByNameAndIso3Query getByNameAndIso3Query = new GetByNameAndIso3Query();
    getByNameAndIso3Query.setName(name);
    getByNameAndIso3Query.setIso3Language(language4);

    String queryContent = getByNameAndIso3QueryTester.write(getByNameAndIso3Query).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/getActionTrlByNameAndIso3")
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
    ServiceResult<ActionTrlDTO> actionDTOServiceResult =
        serviceResultForActionTrlDTOTester.parse(resultContentString).getObject();
    assertTrue(actionDTOServiceResult.getIsSuccess());
    assertNotNull(actionDTOServiceResult.getData());

    assertEquals(name, actionDTOServiceResult.getData().getName());

    debug(loggerPrefix, "Result : " + actionDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetActionTrlByNameAndIso3 with an unknow language")
  void testGetActionTrlByNameAndIso3WithUnknowLanguage() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetActionTrlByNameAndIso3");
    String testSuffix = "-testGetActionTrlByNameAndIso3";

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
                MockMvcRequestBuilders.post("/api/actionService/getActionTrlByNameAndIso3")
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
    ServiceResult<ActionTrlDTO> actionDTOServiceResult =
        serviceResultForActionTrlDTOTester.parse(resultContentString).getObject();
    assertTrue(actionDTOServiceResult.getIsSuccess());
    assertNotNull(actionDTOServiceResult.getData());

    assertEquals(name, actionDTOServiceResult.getData().getName());
    assertEquals(name, actionDTOServiceResult.getData().getValue());

    debug(loggerPrefix, "Result : " + actionDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test FindAnyMatching (search name)")
  void testFindAnyMatchingSearchByName() throws Exception {
    String loggerPrefix = getLoggerPrefix("testFindAnyMatchingSearchByName");
    String testSuffix = "-testFindAnyMatchingSearchByName";

    // given
    String name = dataName + testSuffix;
    createDummyActionDTOWithTranslations(
        name,
        dataDesc + testSuffix,
        valueName + testSuffix,
        tooltipName + testSuffix,
        language1,
        language2);
    FindAnyMatchingQuery findAnyMatchingQuery = new FindAnyMatchingQuery();
    findAnyMatchingQuery.setFilter(name);

    String queryContent = findAnyMatchingQueryTester.write(findAnyMatchingQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/findAnyMatching")
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
    ServiceResult<PageDTO<ActionDTO>> actionPageServiceResult =
        serviceResultForActionDTOPageTester.parse(resultContentString).getObject();
    assertTrue(actionPageServiceResult.getIsSuccess());
    assertNotNull(actionPageServiceResult.getData());

    assertEquals(1, actionPageServiceResult.getData().getTotalElements());
    assertEquals(name, actionPageServiceResult.getData().getContent().get(0).getName());

    debug(loggerPrefix, "Result : " + actionPageServiceResult.getData());
  }

  @Test
  @DisplayName("Test FindAnyMatching (search value)")
  void testFindAnyMatchingSearchByValue() throws Exception {
    String loggerPrefix = getLoggerPrefix("testFindAnyMatchingSearchByValue");
    String testSuffix = "-testFindAnyMatchingSearchByValue";

    // given
    String name = dataName + testSuffix;
    createDummyActionDTOWithTranslations(
        name,
        dataDesc + testSuffix,
        valueName + testSuffix,
        tooltipName + testSuffix,
        language1,
        language2);
    FindAnyMatchingQuery findAnyMatchingQuery = new FindAnyMatchingQuery();
    findAnyMatchingQuery.setFilter(valueName + testSuffix);

    String queryContent = findAnyMatchingQueryTester.write(findAnyMatchingQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/findAnyMatching")
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
    ServiceResult<PageDTO<ActionDTO>> actionPageServiceResult =
        serviceResultForActionDTOPageTester.parse(resultContentString).getObject();
    assertTrue(actionPageServiceResult.getIsSuccess());
    assertNotNull(actionPageServiceResult.getData());

    assertEquals(1, actionPageServiceResult.getData().getTotalElements());
    assertEquals(name, actionPageServiceResult.getData().getContent().get(0).getName());

    debug(loggerPrefix, "Result : " + actionPageServiceResult.getData());
  }

  @Test
  @DisplayName("Test CountAnyMatching")
  void testCountAnyMatching() throws Exception {
    String loggerPrefix = getLoggerPrefix("testCountAnyMatching");
    String testSuffix = "-testCountAnyMatching";

    // given
    String name = dataName + testSuffix;
    createDummyActionDTOWithTranslations(
        name,
        dataDesc + testSuffix,
        valueName + testSuffix,
        tooltipName + testSuffix,
        language1,
        language2);
    CountAnyMatchingQuery countAnyMatchingQuery = new CountAnyMatchingQuery();
    countAnyMatchingQuery.setFilter(name);

    String queryContent = countAnyMatchingQueryTester.write(countAnyMatchingQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/countAnyMatching")
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
    ServiceResult<Long> actionDTOServiceResult =
        serviceResultForLongTester.parse(resultContentString).getObject();
    assertTrue(actionDTOServiceResult.getIsSuccess());
    assertNotNull(actionDTOServiceResult.getData());

    assertEquals(1, actionDTOServiceResult.getData());

    debug(loggerPrefix, "Result : " + actionDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetAll")
  void testGetAll() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetAll");
    String testSuffix = "-testGetAll";

    // given
    String name = dataName + testSuffix;
    createDummyActionDTO(name, dataDesc + testSuffix);
    BaseRemoteQuery baseRemoteQuery = new BaseRemoteQuery();

    String queryContent = baseRemoteQueryTester.write(baseRemoteQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/getAll")
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
    ServiceResult<List<ActionDTO>> actionDTOListServiceResult =
        serviceResultForActionDTOListTester.parse(resultContentString).getObject();
    assertTrue(actionDTOListServiceResult.getIsSuccess());
    assertNotNull(actionDTOListServiceResult.getData());
    assertEquals(actionRepository.count(), actionDTOListServiceResult.getData().size());

    debug(loggerPrefix, "Result : " + actionDTOListServiceResult.getData());
  }

  @Test
  @DisplayName("Test Delete (with no translation)")
  void testDeleteWithNoTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testDeleteWithNoTranslation");
    String testSuffix = "-testDeleteWithNoTranslation";

    // given
    ActionDTO newAction = new ActionDTO();
    newAction.setName(dataName + testSuffix);
    newAction.setCategory(dataDesc + testSuffix);

    UUID actionId = postNewCreateAction(newAction);

    DeleteByIdQuery deleteByIdQuery = new DeleteByIdQuery();
    deleteByIdQuery.setId(actionId);

    String queryContent = deleteByIdQueryTester.write(deleteByIdQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/delete")
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

    assertThat(elementRepository.findById(actionId)).isEmpty();

    debug(loggerPrefix, "Result : " + voidServiceResult.getData());
  }

  @Test
  @DisplayName("Test Delete (with translations)")
  void testDeleteWithTranslations() throws Exception {
    String loggerPrefix = getLoggerPrefix("testDeleteWithTranslations");
    String testSuffix = "-testDeleteWithTranslations";

    // given
    ActionDTO newAction = new ActionDTO();
    newAction.setName(dataName + testSuffix);
    newAction.setCategory(dataDesc + testSuffix);

    ActionTrlDTO actionTrlDTO = new ActionTrlDTO();
    actionTrlDTO.setValue(valueName + testSuffix);
    actionTrlDTO.setTooltip(tooltipName + testSuffix);
    actionTrlDTO.setIso3Language(language1);
    newAction.getTranslations().add(actionTrlDTO);

    UUID actionId = postNewCreateAction(newAction);

    DeleteByIdQuery deleteByIdQuery = new DeleteByIdQuery();
    deleteByIdQuery.setId(actionId);

    String queryContent = deleteByIdQueryTester.write(deleteByIdQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/delete")
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

    assertThat(elementRepository.findById(actionId)).isEmpty();
    assertEquals(0, elementTrlRepository.findByParentId(actionId).size());

    debug(loggerPrefix, "Result : " + voidServiceResult.getData());
  }

  protected UUID createDummyActionDTO(String name, String category) {
    Action action = new Action();
    action.setId(UUID.randomUUID());
    action.setName(name);
    action.setCategory(category);

    return actionRepository.save(action).getId();
  }

  protected UUID createDummyActionDTOWithTranslations(
      String name, String category, String value, String tooltip, String iso1, String iso2) {
    Action action = new Action();
    action.setId(UUID.randomUUID());
    action.setName(name);
    action.setCategory(category);

    ActionTrl actionTrl1 = new ActionTrl();
    actionTrl1.setId(UUID.randomUUID());
    actionTrl1.setValue(value + "-1");
    actionTrl1.setTooltip(tooltip + "-1");
    actionTrl1.setIso3Language(iso1);

    ActionTrl actionTrl2 = new ActionTrl();
    actionTrl2.setId(UUID.randomUUID());
    actionTrl2.setValue(value + "-2");
    actionTrl2.setTooltip(tooltip + "-2");
    actionTrl2.setIso3Language(iso2);

    action.getTranslations().put(actionTrl1.getIso3Language(), actionTrl1);
    action.getTranslations().put(actionTrl2.getIso3Language(), actionTrl2);

    return actionRepository.save(action).getId();
  }

  private UUID postNewCreateAction(ActionDTO action) throws Exception {
    String loggerPrefix = getLoggerPrefix("postNewCreateAction");
    String testSuffix = "-postNewCreateAction";

    // given
    SaveQuery<ActionDTO> saveQuery = new SaveQuery<>();
    saveQuery.setEntity(action);

    String queryContent = saveQueryTester.write(saveQuery).getJson();

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/actionService/save")
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
    ServiceResult<ActionDTO> actionDTOServiceResult =
        serviceResultForActionDTOTester.parse(resultContentString).getObject();
    assertTrue(actionDTOServiceResult.getIsSuccess());
    assertEquals(action.getName(), actionDTOServiceResult.getData().getName());
    assertEquals(action.getCategory(), actionDTOServiceResult.getData().getCategory());

    return actionDTOServiceResult.getData().getId();
  }
}