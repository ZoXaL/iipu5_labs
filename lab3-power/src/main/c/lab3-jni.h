/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_zoxal_labs_iapd_power_NativeFacade */

#ifndef _Included_com_zoxal_labs_iapd_power_NativeFacade
#define _Included_com_zoxal_labs_iapd_power_NativeFacade
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_zoxal_labs_iapd_power_NativeFacade
 * Method:    getSleepTimeout
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_getSleepTimeout
  (JNIEnv *, jclass);

/*
 * Class:     com_zoxal_labs_iapd_power_NativeFacade
 * Method:    setSleepTimeout
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_setSleepTimeout
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_zoxal_labs_iapd_power_NativeFacade
 * Method:    getCurrentCapacity
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_getCurrentCapacity
  (JNIEnv *, jclass);

/*
 * Class:     com_zoxal_labs_iapd_power_NativeFacade
 * Method:    getTotalCapacity
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_getTotalCapacity
  (JNIEnv *, jclass);

/*
 * Class:     com_zoxal_labs_iapd_power_NativeFacade
 * Method:    waitPowerCapacityChange
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_waitPowerCapacityChange
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_zoxal_labs_iapd_power_NativeFacade
 * Method:    waitPowerSupplyChange
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_waitPowerSupplyChange
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_zoxal_labs_iapd_power_NativeFacade
 * Method:    waitPowerSupplyChange
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_getCurrentSupplier (JNIEnv*, jclass);

#ifdef __cplusplus
}
#endif
#endif