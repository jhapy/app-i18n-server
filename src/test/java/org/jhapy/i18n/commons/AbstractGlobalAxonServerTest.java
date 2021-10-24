package org.jhapy.i18n.commons;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.*;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.i18n.repository.*;
import org.jhapy.i18n.testcontainers.AxonServerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"axon.axonserver.servers=${AXON_SERVERS}"})
@Testcontainers
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ActiveProfiles({"local", "test"})
public abstract class AbstractGlobalAxonServerTest implements HasLogger {

  @Container public static AxonServerContainer axonServer = AxonServerContainer.getGlobalInstance();

  @Autowired protected MockMvc mockMvc;

  @Autowired protected JacksonTester<SaveQuery> saveQueryTester;
  @Autowired protected JacksonTester<SaveAllQuery> saveAllQueryTester;
  @Autowired protected JacksonTester<GetByIdQuery> getByIdQueryTester;
  @Autowired protected JacksonTester<BaseRemoteQuery> baseRemoteQueryTester;
  @Autowired protected JacksonTester<DeleteByIdQuery> deleteByIdQueryTester;
  @Autowired protected JacksonTester<GetByNameQuery> getByNameQueryTester;
  @Autowired protected JacksonTester<FindByIso3Query> findByIso3QueryTester;
  @Autowired protected JacksonTester<GetByNameAndIso3Query> getByNameAndIso3QueryTester;
  @Autowired protected JacksonTester<FindAnyMatchingQuery> findAnyMatchingQueryTester;
  @Autowired protected JacksonTester<CountAnyMatchingQuery> countAnyMatchingQueryTester;

  @Autowired protected JacksonTester<ServiceResult<UUID>> serviceResultForUUIDTester;
  @Autowired protected JacksonTester<ServiceResult<Long>> serviceResultForLongTester;
  @Autowired protected JacksonTester<ServiceResult<Void>> serviceResultForVoidTester;
  @Autowired protected JacksonTester<ServiceResult<String>> serviceResultForStringTester;

  @Autowired protected ElementRepository elementRepository;
  @Autowired protected ElementTrlRepository elementTrlRepository;
  @Autowired protected ElementLookupRepository elementLookupRepository;

  @Autowired protected ActionRepository actionRepository;
  @Autowired protected ActionTrlRepository actionTrlRepository;
  @Autowired protected ActionLookupRepository actionLookupRepository;

  @Autowired protected MessageRepository messageRepository;
  @Autowired protected MessageTrlRepository messageTrlRepository;
  @Autowired protected MessageLookupRepository messageLookupRepository;

  @Autowired protected CommandGateway commandGateway;
}