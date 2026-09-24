package com.msb.hjy.ai.client;

/** 仅在同步工具执行期间设置，finally 清理，不依赖 Servlet 线程传播。 */
public final class CallerContext {
    public static final String AUTHORIZATION = "hjy.authorization";
    public static final String USER_NAME = "hjy.userName";
    public static final String USER_MESSAGE = "hjy.userMessage";
    public static final String CONVERSATION_ID = "hjy.conversationId";
    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();
    private CallerContext() { }
    public static String current() { return TOKEN.get(); }
    public static String install(String token) {
        String previous = TOKEN.get();
        TOKEN.set(token);
        return previous;
    }
    public static void restore(String previous) {
        if (previous == null) TOKEN.remove(); else TOKEN.set(previous);
    }
}
