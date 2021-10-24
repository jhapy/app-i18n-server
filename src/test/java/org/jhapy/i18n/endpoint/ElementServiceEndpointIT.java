package org.jhapy.i18n.endpoint;

import com.github.javafaker.Faker;
import org.jhapy.dto.domain.i18n.ElementDTO;
import org.jhapy.dto.domain.i18n.ElementTrlDTO;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.*;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.GetElementTrlQuery;
import org.jhapy.dto.utils.PageDTO;
import org.jhapy.i18n.commons.AbstractGlobalAxonServerTest;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
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

public class ElementServiceEndpointIT extends AbstractGlobalAxonServerTest {
  private static final String TEST_USER_ID = "clavaud@me.com";

  private final String dataName = Faker.instance().aviation().airport();
  private final String dataDesc = Faker.instance().aviation().METAR();
  private final String valueName = Faker.instance().lorem().word();
  private final String tooltipName = Faker.instance().lorem().sentence();

  private final String language1 = Faker.instance().country().countryCode3();
  private final String language2 = Faker.instance().country().countryCode3();
  private final String language3 = Faker.instance().country().countryCode3();
  private final String language4 = Faker.instance().country().countryCode3();

  @Autowired private JacksonTester<ServiceResult<ElementDTO>> serviceResultForElementDTOTester;

  @Autowired
  private JacksonTester<ServiceResult<ElementTrlDTO>> serviceResultForElementTrlDTOTester;

  @Autowired
  private JacksonTester<ServiceResult<List<ElementTrlDTO>>> serviceResultForElementTrlDTOListTester;

  @Autowired
  private JacksonTester<ServiceResult<List<ElementDTO>>> serviceResultForElementDTOListTester;

  @Autowired
  private JacksonTester<ServiceResult<PageDTO<ElementDTO>>> serviceResultForElementDTOPageTester;

  @Autowired private JacksonTester<GetElementTrlQuery> getElementTrlQueryTester;

  @BeforeAll
  public static void setUp() {}

  @AfterAll
  public static void cleanUp() {}

  @Test
  @DisplayName("Test Save an Element without Translation (ElementTrl)")
  void testSaveWithNoTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveWithNoTranslation");
    String testSuffix = "-testSaveWithNoTranslation";

    // given
    ElementDTO newElement = new ElementDTO();
    newElement.setName(dataName + testSuffix);
    newElement.setCategory(dataDesc + testSuffix);

    SaveQuery<ElementDTO> saveQuery = new SaveQuery<>();
    saveQuery.setEntity(newElement);

    String queryContent = saveQueryTester.write(saveQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/save")
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
    ServiceResult<ElementDTO> elementDTOServiceResult =
        serviceResultForElementDTOTester.parse(resultContentString).getObject();
    assertTrue(elementDTOServiceResult.getIsSuccess());
    assertEquals(newElement.getName(), elementDTOServiceResult.getData().getName());
    assertEquals(newElement.getCategory(), elementDTOServiceResult.getData().getCategory());

    debug(loggerPrefix, "Result : " + elementDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test Save an Element with Translation (ElementTrl)")
  void testSaveWithTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveWithTranslation");
    String testSuffix = "-testSaveWithTranslation";

    // given
    ElementDTO newElement = new ElementDTO();
    newElement.setName(dataName + testSuffix);
    newElement.setCategory(dataDesc + testSuffix);

    ElementTrlDTO elementTrlDTO = new ElementTrlDTO();
    elementTrlDTO.setValue(valueName + testSuffix);
    elementTrlDTO.setTooltip(tooltipName + testSuffix);
    elementTrlDTO.setIso3Language(language1);
    newElement.getTranslations().add(elementTrlDTO);

    SaveQuery<ElementDTO> saveQuery = new SaveQuery<>();
    saveQuery.setEntity(newElement);

    String queryContent = saveQueryTester.write(saveQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/save")
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
    ServiceResult<ElementDTO> elementDTOServiceResult =
        serviceResultForElementDTOTester.parse(resultContentString).getObject();
    assertTrue(elementDTOServiceResult.getIsSuccess());
    assertEquals(newElement.getName(), elementDTOServiceResult.getData().getName());
    assertEquals(newElement.getCategory(), elementDTOServiceResult.getData().getCategory());
    assertNotNull(newElement.getTranslations());
    assertEquals(1, elementDTOServiceResult.getData().getTranslations().size());
    ElementTrlDTO resultElementTrlDTO = elementDTOServiceResult.getData().getTranslations().get(0);
    assertEquals(newElement.getName(), resultElementTrlDTO.getName());
    assertEquals(elementTrlDTO.getValue(), resultElementTrlDTO.getValue());
    assertEquals(elementTrlDTO.getTooltip(), resultElementTrlDTO.getTooltip());

    debug(loggerPrefix, "Result : " + elementDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test Save several Elements without Translation (ElementTrl)")
  void testSaveAllWithNoTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveAllWithNoTranslation");
    String testSuffix = "-testSaveAllWithNoTranslation";

    // given
    ElementDTO newElement1 = new ElementDTO();
    newElement1.setName(dataName + testSuffix);
    newElement1.setCategory(dataDesc + testSuffix);

    ElementDTO newElement2 = new ElementDTO();
    newElement2.setName(dataName + "-1" + testSuffix);
    newElement2.setCategory(dataDesc + "-1" + testSuffix);

    SaveAllQuery<ElementDTO> saveAllQuery = new SaveAllQuery<>();
    saveAllQuery.setEntity(Arrays.asList(newElement1, newElement2));

    String queryContent = saveAllQueryTester.write(saveAllQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/saveAll")
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
    ServiceResult<List<ElementDTO>> elementDTOListServiceResult =
        serviceResultForElementDTOListTester.parse(resultContentString).getObject();
    assertTrue(elementDTOListServiceResult.getIsSuccess());
    assertEquals(2, elementDTOListServiceResult.getData().size());

    debug(loggerPrefix, "Result : " + elementDTOListServiceResult.getData());
  }

  @Test
  @DisplayName("Test Save several Elements without Translation (ElementTrl)")
  void testSaveAllWithTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testSaveAllWithTranslation");
    String testSuffix = "-testSaveAllWithTranslation";

    // given
    ElementDTO newElement1 = new ElementDTO();
    newElement1.setName(dataName + testSuffix);
    newElement1.setCategory(dataDesc + testSuffix);

    ElementTrlDTO elementTrlDTO1 = new ElementTrlDTO();
    elementTrlDTO1.setValue(valueName + testSuffix);
    elementTrlDTO1.setTooltip(tooltipName + testSuffix);
    elementTrlDTO1.setIso3Language(language1);
    newElement1.getTranslations().add(elementTrlDTO1);

    ElementDTO newElement2 = new ElementDTO();
    newElement2.setName(dataName + "-1" + testSuffix);
    newElement2.setCategory(dataDesc + "-1" + testSuffix);

    ElementTrlDTO elementTrlDTO2 = new ElementTrlDTO();
    elementTrlDTO2.setValue(valueName + testSuffix);
    elementTrlDTO2.setTooltip(tooltipName + testSuffix);
    elementTrlDTO2.setIso3Language(language1);
    newElement2.getTranslations().add(elementTrlDTO2);

    SaveAllQuery<ElementDTO> saveAllQuery = new SaveAllQuery<>();
    saveAllQuery.setEntity(Arrays.asList(newElement1, newElement2));

    String queryContent = saveAllQueryTester.write(saveAllQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/saveAll")
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
    ServiceResult<List<ElementDTO>> elementDTOListServiceResult =
        serviceResultForElementDTOListTester.parse(resultContentString).getObject();
    assertTrue(elementDTOListServiceResult.getIsSuccess());
    assertEquals(2, elementDTOListServiceResult.getData().size());
    assertEquals(1, elementDTOListServiceResult.getData().get(0).getTranslations().size());
    assertEquals(1, elementDTOListServiceResult.getData().get(1).getTranslations().size());
    debug(loggerPrefix, "Result : " + elementDTOListServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetByName")
  void testGetByName() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetByName");
    String testSuffix = "-testGetByName";

    // given
    String name = dataName + testSuffix;
    createDummyElementDTO(name, dataDesc + testSuffix);
    GetByNameQuery getByNameQuery = new GetByNameQuery();
    getByNameQuery.setName(name);

    String queryContent = getByNameQueryTester.write(getByNameQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/getByName")
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
    ServiceResult<ElementDTO> elementDTOServiceResult =
        serviceResultForElementDTOTester.parse(resultContentString).getObject();
    assertTrue(elementDTOServiceResult.getIsSuccess());
    assertNotNull(elementDTOServiceResult.getData());
    assertEquals(name, elementDTOServiceResult.getData().getName());

    debug(loggerPrefix, "Result : " + elementDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetById")
  void testGetById() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetById");
    String testSuffix = "-testGetById";

    // given
    String name = dataName + testSuffix;
    UUID createdElementId = createDummyElementDTO(name, dataDesc + testSuffix);
    GetByIdQuery getByIdQuery = new GetByIdQuery();
    getByIdQuery.setId(createdElementId);

    String queryContent = getByIdQueryTester.write(getByIdQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/getById")
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
    ServiceResult<ElementDTO> elementDTOServiceResult =
        serviceResultForElementDTOTester.parse(resultContentString).getObject();
    assertTrue(elementDTOServiceResult.getIsSuccess());
    assertNotNull(elementDTOServiceResult.getData());
    assertEquals(name, elementDTOServiceResult.getData().getName());

    debug(loggerPrefix, "Result : " + elementDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetElementTrls")
  void testGetElementTrls() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetElementTrls");
    String testSuffix = "-testGetElementTrls";

    // given
    String name = dataName + testSuffix;
    UUID createdElementId =
        createDummyElementDTOWithTranslations(
            name,
            dataDesc + testSuffix,
            valueName + testSuffix,
            tooltipName + testSuffix,
            language1,
            language2);
    GetElementTrlQuery getElementTrlQuery = new GetElementTrlQuery();
    getElementTrlQuery.setElementId(createdElementId);

    String queryContent = getElementTrlQueryTester.write(getElementTrlQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/getElementTrls")
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
    ServiceResult<List<ElementTrlDTO>> elementDTOServiceResult =
        serviceResultForElementTrlDTOListTester.parse(resultContentString).getObject();
    assertTrue(elementDTOServiceResult.getIsSuccess());
    assertNotNull(elementDTOServiceResult.getData());

    assertEquals(2, elementDTOServiceResult.getData().size());

    debug(loggerPrefix, "Result : " + elementDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test FindByIso3")
  void testFindByIso3() throws Exception {
    String loggerPrefix = getLoggerPrefix("testFindByIso3");
    String testSuffix = "-testFindByIso3";

    // given
    String name = dataName + testSuffix;
    createDummyElementDTOWithTranslations(
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
                MockMvcRequestBuilders.post("/api/elementService/findByIso3")
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
    ServiceResult<List<ElementTrlDTO>> elementDTOServiceResult =
        serviceResultForElementTrlDTOListTester.parse(resultContentString).getObject();
    assertTrue(elementDTOServiceResult.getIsSuccess());
    assertNotNull(elementDTOServiceResult.getData());

    assertEquals(1, elementDTOServiceResult.getData().size());
    assertEquals(name, elementDTOServiceResult.getData().get(0).getName());

    debug(loggerPrefix, "Result : " + elementDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetElementTrlByNameAndIso3")
  void testGetElementTrlByNameAndIso3() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetElementTrlByNameAndIso3");
    String testSuffix = "-testGetElementTrlByNameAndIso3";

    // given
    String name = dataName + testSuffix;
    createDummyElementDTOWithTranslations(
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
                MockMvcRequestBuilders.post("/api/elementService/getElementTrlByNameAndIso3")
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
    ServiceResult<ElementTrlDTO> elementDTOServiceResult =
        serviceResultForElementTrlDTOTester.parse(resultContentString).getObject();
    assertTrue(elementDTOServiceResult.getIsSuccess());
    assertNotNull(elementDTOServiceResult.getData());

    assertEquals(name, elementDTOServiceResult.getData().getName());

    debug(loggerPrefix, "Result : " + elementDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetElementTrlByNameAndIso3 with an unknown language")
  void testGetElementTrlByNameAndIso3WithUnknowLanguage() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetElementTrlByNameAndIso3");
    String testSuffix = "-testGetElementTrlByNameAndIso3";

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
                MockMvcRequestBuilders.post("/api/elementService/getElementTrlByNameAndIso3")
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
    ServiceResult<ElementTrlDTO> elementDTOServiceResult =
        serviceResultForElementTrlDTOTester.parse(resultContentString).getObject();
    assertTrue(elementDTOServiceResult.getIsSuccess());
    assertNotNull(elementDTOServiceResult.getData());

    assertEquals(name, elementDTOServiceResult.getData().getName());
    assertEquals(name, elementDTOServiceResult.getData().getValue());

    debug(loggerPrefix, "Result : " + elementDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test FindAnyMatching (search name)")
  void testFindAnyMatchingSearchByName() throws Exception {
    String loggerPrefix = getLoggerPrefix("testFindAnyMatchingSearchByName");
    String testSuffix = "-testFindAnyMatchingSearchByName";

    // given
    String name = dataName + testSuffix;
    createDummyElementDTOWithTranslations(
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
                MockMvcRequestBuilders.post("/api/elementService/findAnyMatching")
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
    ServiceResult<PageDTO<ElementDTO>> elementPageServiceResult =
        serviceResultForElementDTOPageTester.parse(resultContentString).getObject();
    assertTrue(elementPageServiceResult.getIsSuccess());
    assertNotNull(elementPageServiceResult.getData());

    assertEquals(1, elementPageServiceResult.getData().getTotalElements());
    assertEquals(name, elementPageServiceResult.getData().getContent().get(0).getName());

    debug(loggerPrefix, "Result : " + elementPageServiceResult.getData());
  }

  @Test
  @DisplayName("Test FindAnyMatching (search value)")
  void testFindAnyMatchingSearchByValue() throws Exception {
    String loggerPrefix = getLoggerPrefix("testFindAnyMatchingSearchByValue");
    String testSuffix = "-testFindAnyMatchingSearchByValue";

    // given
    String name = dataName + testSuffix;
    createDummyElementDTOWithTranslations(
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
                MockMvcRequestBuilders.post("/api/elementService/findAnyMatching")
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
    ServiceResult<PageDTO<ElementDTO>> elementPageServiceResult =
        serviceResultForElementDTOPageTester.parse(resultContentString).getObject();
    assertTrue(elementPageServiceResult.getIsSuccess());
    assertNotNull(elementPageServiceResult.getData());

    assertEquals(1, elementPageServiceResult.getData().getTotalElements());
    assertEquals(name, elementPageServiceResult.getData().getContent().get(0).getName());

    debug(loggerPrefix, "Result : " + elementPageServiceResult.getData());
  }

  @Test
  @DisplayName("Test CountAnyMatching")
  void testCountAnyMatching() throws Exception {
    String loggerPrefix = getLoggerPrefix("testCountAnyMatching");
    String testSuffix = "-testCountAnyMatching";

    // given
    String name = dataName + testSuffix;
    createDummyElementDTOWithTranslations(
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
                MockMvcRequestBuilders.post("/api/elementService/countAnyMatching")
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
    ServiceResult<Long> elementDTOServiceResult =
        serviceResultForLongTester.parse(resultContentString).getObject();
    assertTrue(elementDTOServiceResult.getIsSuccess());
    assertNotNull(elementDTOServiceResult.getData());

    assertEquals(1, elementDTOServiceResult.getData());

    debug(loggerPrefix, "Result : " + elementDTOServiceResult.getData());
  }

  @Test
  @DisplayName("Test GetAll")
  void testGetAll() throws Exception {
    String loggerPrefix = getLoggerPrefix("testGetAll");
    String testSuffix = "-testGetAll";

    // given
    String name = dataName + testSuffix;
    createDummyElementDTO(name, dataDesc + testSuffix);
    BaseRemoteQuery baseRemoteQuery = new BaseRemoteQuery();

    String queryContent = baseRemoteQueryTester.write(baseRemoteQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/getAll")
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
    ServiceResult<List<ElementDTO>> elementDTOListServiceResult =
        serviceResultForElementDTOListTester.parse(resultContentString).getObject();
    assertTrue(elementDTOListServiceResult.getIsSuccess());
    assertNotNull(elementDTOListServiceResult.getData());
    assertEquals(elementRepository.count(), elementDTOListServiceResult.getData().size());

    debug(loggerPrefix, "Result : " + elementDTOListServiceResult.getData());
  }

  @Test
  @DisplayName("Test Delete (with no translation)")
  void testDeleteWithNoTranslation() throws Exception {
    String loggerPrefix = getLoggerPrefix("testDeleteWithNoTranslation");
    String testSuffix = "-testDeleteWithNoTranslation";

    // given
    ElementDTO newElement = new ElementDTO();
    newElement.setName(dataName + testSuffix);
    newElement.setCategory(dataDesc + testSuffix);

    UUID elementId = postNewCreateElement(newElement);

    DeleteByIdQuery deleteByIdQuery = new DeleteByIdQuery();
    deleteByIdQuery.setId(elementId);

    String queryContent = deleteByIdQueryTester.write(deleteByIdQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/delete")
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

    assertThat(elementRepository.findById(elementId)).isEmpty();

    debug(loggerPrefix, "Result : " + voidServiceResult.getData());
  }

  @Test
  @DisplayName("Test Delete (with translations)")
  void testDeleteWithTranslations() throws Exception {
    String loggerPrefix = getLoggerPrefix("testDeleteWithTranslations");
    String testSuffix = "-testDeleteWithTranslations";

    // given
    ElementDTO newElement = new ElementDTO();
    newElement.setName(dataName + testSuffix);
    newElement.setCategory(dataDesc + testSuffix);

    ElementTrlDTO elementTrlDTO = new ElementTrlDTO();
    elementTrlDTO.setValue(valueName + testSuffix);
    elementTrlDTO.setTooltip(tooltipName + testSuffix);
    elementTrlDTO.setIso3Language(language1);
    newElement.getTranslations().add(elementTrlDTO);

    UUID elementId = postNewCreateElement(newElement);

    DeleteByIdQuery deleteByIdQuery = new DeleteByIdQuery();
    deleteByIdQuery.setId(elementId);

    String queryContent = deleteByIdQueryTester.write(deleteByIdQuery).getJson();
    debug(loggerPrefix, "Json Input = {0}", queryContent);

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/delete")
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

    assertThat(elementRepository.findById(elementId)).isEmpty();
    assertEquals(0, elementTrlRepository.findByParentId(elementId).size());

    debug(loggerPrefix, "Result : " + voidServiceResult.getData());
  }

  protected UUID createDummyElementDTO(String name, String category) {
    Element element = new Element();
    element.setId(UUID.randomUUID());
    element.setName(name);
    element.setCategory(category);

    return elementRepository.save(element).getId();
  }

  protected UUID createDummyElementDTOWithTranslations(
      String name, String category, String value, String tooltip, String iso1, String iso2) {
    Element element = new Element();
    element.setId(UUID.randomUUID());
    element.setName(name);
    element.setCategory(category);

    ElementTrl elementTrl1 = new ElementTrl();
    elementTrl1.setId(UUID.randomUUID());
    elementTrl1.setValue(value + "-1");
    elementTrl1.setTooltip(tooltip + "-1");
    elementTrl1.setIso3Language(iso1);

    ElementTrl elementTrl2 = new ElementTrl();
    elementTrl2.setId(UUID.randomUUID());
    elementTrl2.setValue(value + "-2");
    elementTrl2.setTooltip(tooltip + "-2");
    elementTrl2.setIso3Language(iso2);

    element.getTranslations().put(elementTrl1.getIso3Language(), elementTrl1);
    element.getTranslations().put(elementTrl2.getIso3Language(), elementTrl2);

    return elementRepository.save(element).getId();
  }

  private UUID postNewCreateElement(ElementDTO element) throws Exception {
    String loggerPrefix = getLoggerPrefix("postNewCreateElement");
    String testSuffix = "-postNewCreateElement";

    // given
    SaveQuery<ElementDTO> saveQuery = new SaveQuery<>();
    saveQuery.setEntity(element);

    String queryContent = saveQueryTester.write(saveQuery).getJson();

    // when
    MvcResult result =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/elementService/save")
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
    ServiceResult<ElementDTO> elementDTOServiceResult =
        serviceResultForElementDTOTester.parse(resultContentString).getObject();
    assertTrue(elementDTOServiceResult.getIsSuccess());
    assertEquals(element.getName(), elementDTOServiceResult.getData().getName());
    assertEquals(element.getCategory(), elementDTOServiceResult.getData().getCategory());

    return elementDTOServiceResult.getData().getId();
  }
}