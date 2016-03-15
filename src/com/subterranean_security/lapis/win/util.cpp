#include <windows.h>
#include <com_subterranean_security_crimson_client_Native.h>

JNIEXPORT jstring JNICALL Java_com_subterranean_1security_crimson_client_Native_getActiveWindow(JNIEnv *env, jclass cls){
          char title[256];
          GetWindowText(GetForegroundWindow(), title, sizeof(title));
          return env->NewStringUTF(title);
}

JNIEXPORT jlong JNICALL Java_com_subterranean_1security_crimson_client_Native_getSystemUptime(JNIEnv *env, jclass cls){
          DWORD uptime = GetTickCount();
          return uptime;
}
