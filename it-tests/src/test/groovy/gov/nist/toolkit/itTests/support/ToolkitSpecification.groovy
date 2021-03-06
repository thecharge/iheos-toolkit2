package gov.nist.toolkit.itTests.support

import gov.nist.toolkit.adt.ListenerFactory
import gov.nist.toolkit.configDatatypes.client.Pid
import gov.nist.toolkit.grizzlySupport.GrizzlyController
import gov.nist.toolkit.installation.Installation
import gov.nist.toolkit.results.client.Result
import gov.nist.toolkit.results.client.TestInstance
import gov.nist.toolkit.results.client.TestLogs
import gov.nist.toolkit.services.server.ToolkitApi
import gov.nist.toolkit.services.server.UnitTestEnvironmentManager
import gov.nist.toolkit.session.server.Session
import gov.nist.toolkit.toolkitApi.SimulatorBuilder
import gov.nist.toolkit.toolkitServicesCommon.SimId
import org.junit.Rule
import org.junit.rules.TestName
import spock.lang.Shared
import spock.lang.Specification
import org.apache.commons.io.FileUtils

/**
 *
 */

class ToolkitSpecification extends Specification {
    @Rule TestName name = new TestName()
    // these are usable by the specification that extend this class
    @Shared GrizzlyController server = null
    @Shared ToolkitApi api
    @Shared Session session
    @Shared String remoteToolkitPort = null

    def setupSpec() {  // there can be multiple setupSpec() fixture methods - they all get run
        session = UnitTestEnvironmentManager.setupLocalToolkit()
        api = UnitTestEnvironmentManager.localToolkitApi()

        cleanupDir()
    }

    def setup() {
        println 'Running method: ' + name.methodName
    }

    def cleanupDir() {
        File testDataDir = Installation.instance().propertyServiceManager().getTestLogCache()
        if (testDataDir.exists()) {
            System.out.println("Clearing TEST (testLogCache) data before testing...")
            FileUtils.cleanDirectory(testDataDir)
        }

        testDataDir = Installation.instance().simDbFile()
        if (testDataDir.exists()) {
            System.out.println("Clearing TEST (simdb) data before testing...")
            FileUtils.cleanDirectory(testDataDir)
        }
    }

    def startGrizzly(String port) {
        remoteToolkitPort = port
        server = new GrizzlyController()
        server.start(remoteToolkitPort);
        server.withToolkit()
        Installation.instance().overrideToolkitPort(remoteToolkitPort)  // ignore toolkit.properties

    }

    SimulatorBuilder getSimulatorApi(String remoteToolkitPort) {
        String urlRoot = String.format("http://localhost:%s/xdstools2", remoteToolkitPort)
        new SimulatorBuilder(urlRoot)
    }

    SimulatorBuilder getSimulatorApi(String host, String remoteToolkitPort) {
        String urlRoot = String.format("http://%s:%s/xdstools2", host, remoteToolkitPort)
        new SimulatorBuilder(urlRoot)
    }

    def cleanupSpec() {  // one time shutdown when everything is done
        System.gc()
        if (server) {
            server.stop()
            server = null
        }
        ListenerFactory.terminateAll()
    }

    def initializeRegistryWithPatientId(String testSession, SimId simId, Pid pid) {
        TestInstance testId = new TestInstance("15804")
        List<String> sections = new ArrayList<>()
        sections.add("section")
        Map<String, String> params = new HashMap<>()
        params.put('$patientid$', pid.toString())
        boolean stopOnFirstError = true

        List<Result> results = api.runTest(testSession, simId.fullId, testId, sections, params, stopOnFirstError)

        assert results.size() == 1
        assert results.get(0).passed()
    }

    TestLogs initializeRepository(String testSession, SimId simId, Pid pid, TestInstance testInstance) {
        List<String> sections = new ArrayList<>()
        Map<String, String> params = new HashMap<>()
        params.put('$patientid$', pid.toString())
        boolean stopOnFirstError = true

        List<Result> results = api.runTest(testSession, simId.fullId, testInstance, sections, params, stopOnFirstError)

        TestLogs testLogs = api.getTestLogs(testInstance)

        assert testLogs
        assert results.size() == 1
        assert results.get(0).passed()
        return testLogs
    }
}
