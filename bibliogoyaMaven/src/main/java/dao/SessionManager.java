package dao;

public class SessionManager {
    private static Long clienteId;
    private static boolean isUser;

    public static void setClienteId(Long id) {
        clienteId = id;
    }
    public static Long getClienteId() {
        return clienteId;
    }
	public static boolean isUser() {
		return isUser;
	}
	public static void setIsUser(boolean isUser) {
		SessionManager.isUser = isUser;
	}
}

