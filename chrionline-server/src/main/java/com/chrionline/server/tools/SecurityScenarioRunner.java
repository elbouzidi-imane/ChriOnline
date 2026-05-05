package com.chrionline.server.tools;

import com.chrionline.common.AppConstants;
import com.chrionline.common.Message;
import com.chrionline.common.Protocol;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class SecurityScenarioRunner {
    private SecurityScenarioRunner() {
    }

    public static void main(String[] args) {
        List<Scenario> scenarios = List.of(
                new Scenario(
                        "Public request baseline",
                        SecurityScenarioRunner::buildPublicCatalogRequest,
                        "OK",
                        "Verifie qu'une requete publique legitime fonctionne encore."
                ),
                new Scenario(
                        "Protected request without session token",
                        SecurityScenarioRunner::buildProtectedRequestWithoutSession,
                        "Session manquante",
                        "Doit etre rejetee car l'IP seule ne suffit pas."
                ),
                new Scenario(
                        "Invalid requestId",
                        SecurityScenarioRunner::buildInvalidRequestId,
                        "identifiant de requete invalide",
                        "Doit etre rejetee par le firewall applicatif."
                ),
                new Scenario(
                        "Expired timestamp",
                        SecurityScenarioRunner::buildExpiredTimestampRequest,
                        "horodatage invalide ou expire",
                        "Doit etre rejetee car la requete sort de la fenetre temporelle autorisee."
                ),
                new Scenario(
                        "Oversized payload",
                        SecurityScenarioRunner::buildOversizedPayloadRequest,
                        "charge utile trop volumineuse",
                        "Doit etre rejetee par la limite de taille."
                )
        );

        List<ScenarioResult> results = new ArrayList<>();
        for (Scenario scenario : scenarios) {
            results.add(runSingleScenario(scenario));
        }
        results.add(runReplayScenario());
        printSummary(results);
    }

    private static ScenarioResult runSingleScenario(Scenario scenario) {
        try {
            Message response = sendOnce(scenario.messageSupplier().get());
            boolean matched = responseMatches(response, scenario.expectedFragment());
            return new ScenarioResult(
                    scenario.name(),
                    scenario.description(),
                    scenario.expectedFragment(),
                    summarizeResponse(response),
                    matched
            );
        } catch (Exception e) {
            return new ScenarioResult(
                    scenario.name(),
                    scenario.description(),
                    scenario.expectedFragment(),
                    "EXCEPTION: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                    false
            );
        }
    }

    private static ScenarioResult runReplayScenario() {
        String requestId = UUID.randomUUID().toString();
        Message first = buildReplayCandidate(requestId);
        Message second = buildReplayCandidate(requestId);

        try {
            Message firstResponse = sendOnce(first);
            Message secondResponse = sendOnce(second);
            boolean matched = firstResponse.isOk()
                    && responseMatches(secondResponse, "requete rejouee ou dupliquee");
            String actual = "first=" + summarizeResponse(firstResponse)
                    + " | second=" + summarizeResponse(secondResponse);
            return new ScenarioResult(
                    "Replay requestId",
                    "La premiere requete doit passer, la seconde doit etre rejetee comme replay.",
                    "second contains 'requete rejouee ou dupliquee'",
                    actual,
                    matched
            );
        } catch (Exception e) {
            return new ScenarioResult(
                    "Replay requestId",
                    "La premiere requete doit passer, la seconde doit etre rejetee comme replay.",
                    "second contains 'requete rejouee ou dupliquee'",
                    "EXCEPTION: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                    false
            );
        }
    }

    private static Message sendOnce(Message request) throws Exception {
        boolean tlsEnabled = Boolean.parseBoolean(System.getProperty("chrionline.tls.enabled", "false"));
        try (Socket socket = createSocket(tlsEnabled);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(request);
            out.flush();
            Object response = in.readObject();
            if (!(response instanceof Message message)) {
                throw new IllegalStateException("Reponse serveur invalide");
            }
            return message;
        }
    }

    private static Socket createSocket(boolean tlsEnabled) throws Exception {
        if (!tlsEnabled) {
            return new Socket(AppConstants.HOST, AppConstants.PORT_TCP);
        }
        SocketFactory factory = SSLSocketFactory.getDefault();
        return factory.createSocket(AppConstants.HOST, AppConstants.PORT_TCP);
    }

    private static Message buildPublicCatalogRequest() {
        return new Message(Protocol.GET_PRODUCTS, "");
    }

    private static Message buildProtectedRequestWithoutSession() {
        return new Message(Protocol.GET_CART, "1");
    }

    private static Message buildInvalidRequestId() {
        Message message = new Message(Protocol.GET_PRODUCTS, "");
        message.setRequestId("not-a-uuid");
        return message;
    }

    private static Message buildExpiredTimestampRequest() {
        Message message = new Message(Protocol.GET_PRODUCTS, "");
        message.setTimestamp(System.currentTimeMillis() - Duration.ofMinutes(20).toMillis());
        return message;
    }

    private static Message buildOversizedPayloadRequest() {
        Message message = new Message(Protocol.GET_PRODUCTS, "A".repeat(8_193));
        message.setRequestId(UUID.randomUUID().toString());
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    private static Message buildReplayCandidate(String requestId) {
        Message message = new Message(Protocol.GET_PRODUCTS, "");
        message.setRequestId(requestId);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    private static boolean responseMatches(Message response, String expectedFragment) {
        if (response == null) {
            return false;
        }
        if ("OK".equals(expectedFragment)) {
            return response.isOk();
        }
        return response.isError()
                && response.getPayload() != null
                && response.getPayload().contains(expectedFragment);
    }

    private static String summarizeResponse(Message response) {
        if (response == null) {
            return "null";
        }
        return "status=" + response.getStatus() + ", payload=" + response.getPayload();
    }

    private static void printSummary(List<ScenarioResult> results) {
        System.out.println("=== Security Scenario Runner ===");
        long passed = results.stream().filter(ScenarioResult::passed).count();
        for (ScenarioResult result : results) {
            System.out.println();
            System.out.println("[" + (result.passed() ? "PASS" : "FAIL") + "] " + result.name());
            System.out.println("  Why: " + result.description());
            System.out.println("  Expected: " + result.expected());
            System.out.println("  Actual: " + result.actual());
        }
        System.out.println();
        System.out.println("Summary: " + passed + "/" + results.size() + " scenarios passed.");
    }

    private record Scenario(
            String name,
            Supplier<Message> messageSupplier,
            String expectedFragment,
            String description
    ) {
    }

    private record ScenarioResult(
            String name,
            String description,
            String expected,
            String actual,
            boolean passed
    ) {
    }
}
