package com.db.test;

import com.db.client.JiraClient;
import com.db.lib.SignalHandler;
import com.db.model.SignalAlgoDetail;
import com.db.model.SignalAlgoType;
import com.db.model.SignalSpec;
import com.db.model.algo.AlgoGeneric;
import com.db.model.algo.AlgoParameterized;
import com.db.model.algo.impl.*;
import com.db.service.SignalHandlerImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SignalHandlerServiceTest {
    private SignalHandler signalHandler;
    @Mock
    private JiraClient jiraClient = mock(JiraClient.class);
    private Map<String, AlgoGeneric> algoGenericMap;
    private Map<String, AlgoParameterized> algoParameterizedMap;

    //Test the System.out.println generated by the Algo Lib
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeAll
    public void setUp(){
        signalHandler = new SignalHandlerImpl(jiraClient, algoGenericMap, algoParameterizedMap);
        when(jiraClient.fetchSignalSpecs()).thenReturn(generateSampleSignals());
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @BeforeAll
    public void setupTemplate(){
        algoGenericMap = new HashMap<>();
        algoParameterizedMap = new HashMap<>();
        algoGenericMap.put("do", new AlgoDo());
        algoGenericMap.put("cancel", new AlgoCancel());
        algoGenericMap.put("reverse", new AlgoReverse());
        algoGenericMap.put("submitMarket", new AlgoSubmit());
        algoGenericMap.put("performCalculation", new AlgoCalculate());
        algoGenericMap.put("setup", new AlgoSetup());
        algoParameterizedMap.put("setParam", new AlgoParam());
    }

    @Test
    public void shouldAcceptSignal(){
        signalHandler.handleSignal(1);
    }

    @Test
    public void shouldPrintCorrectAlgoStepsWhenSignalGiven(){
        signalHandler.handleSignal(1);
        assertThat(outputStreamCaptor.toString().trim(), startsWith("setUp"));
        assertThat(outputStreamCaptor.toString().trim(), containsString("setAlgoParam1,60"));
        assertThat(outputStreamCaptor.toString().trim(), containsString("performCalc"));
        assertThat(outputStreamCaptor.toString().trim(), endsWith("submitToMarket"));
    }

    private List<SignalSpec> generateSampleSignals(){
        List<SignalSpec> signalSpecList = new ArrayList<>();
        SignalSpec signal1 = new SignalSpec(1L);
        List<SignalAlgoDetail> signalAlgoDetailList = new ArrayList<>();
        SignalAlgoDetail signalAlgoDetail1 = new SignalAlgoDetail("setup", SignalAlgoType.GENERIC);
        SignalAlgoDetail signalAlgoDetail2 = new SignalAlgoDetail("setParam", SignalAlgoType.PARAM);
        signalAlgoDetail2.setParam1(1);
        signalAlgoDetail2.setParam2(60);

        SignalAlgoDetail signalAlgoDetail3 = new SignalAlgoDetail("performCalculation", SignalAlgoType.GENERIC);
        SignalAlgoDetail signalAlgoDetail4 = new SignalAlgoDetail("submitMarket", SignalAlgoType.GENERIC);
        signalAlgoDetailList.add(signalAlgoDetail1);
        signalAlgoDetailList.add(signalAlgoDetail2);
        signalAlgoDetailList.add(signalAlgoDetail3);
        signalAlgoDetailList.add(signalAlgoDetail4);

        signal1.setAlgoDetailList(signalAlgoDetailList);

        signalSpecList.add(signal1);
        return signalSpecList;
    }
}
