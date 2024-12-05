import { WebView } from 'react-native-webview';
import Constants from 'expo-constants';
import { StyleSheet } from 'react-native';
import React from 'react';
import { Stack } from 'expo-router';
import { AuthWrapper } from '~/components';

export default function ShelfAndInventoryWebView() {
    return (
        <><Stack.Screen
            options={{
                headerShown: false,
            }} />
            <AuthWrapper>


                <WebView
                    style={styles.container}
                    source={{ uri: 'https://circle-help.vercel.app/shelfAndInventory' }} />
            </AuthWrapper >
        </>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        marginTop: Constants.statusBarHeight,
    },
});