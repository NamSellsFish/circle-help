import React, { useState } from "react";
import { View, StyleSheet, Platform, Text } from "react-native";
import { Button, ImagePickerModalT , ImageViewer } from "~/components";
import * as ImagePicker from 'expo-image-picker';
import { ImageSource } from "expo-image";
import { Stack, useRouter } from "expo-router";
import { FontAwesome } from "@expo/vector-icons";
import { useDisclosure } from "~/hooks";

export default function ImagePickerScreen() {
    const [isShowImagePickerModal, imagePickerModalHandlers] = useDisclosure()
    const [selectedImage, setSelectedImage] = useState<string | undefined>(undefined);

    const componentDidMount = async () => {
        if (Platform.OS !== "web") {
            const {
                status,
            } = await ImagePicker.requestMediaLibraryPermissionsAsync();
            if (status !== "granted") {
                alert("Sorry, we need camera roll permissions to make this work!");
            }
        }
    }

    const pickImageGalleryAsync = async () => {
        await componentDidMount();
        let result = await ImagePicker.launchImageLibraryAsync({
            // mediaTypes: 'images',
            allowsEditing: true,
            quality: 1,
        });

        if (!result.canceled) {
            setSelectedImage(result.assets[0].uri);
        } else {
            alert('You did not select any image.');
        }
    };

    const pickImageCameraAsync = async () => {
        await componentDidMount();
        let result = await ImagePicker.launchCameraAsync({
            // mediaTypes: ['images'],
            allowsEditing: true,
            quality: 1,
        });

        if (!result.canceled) {
            
            setSelectedImage(result.assets[0].uri);
        } else {
            alert('You did not take any photo.');
        }
    };



    return (
        <>
            <Stack.Screen
                options={{
                    headerShown: true,
                    headerBackVisible: true,
                    title: '',
                    headerStyle: { backgroundColor: '#25292e', },
                    headerTintColor: 'white',
                }} />


            <View style={styles.container}>
                <View style={styles.imageContainer}>
                    <ImageViewer imgSource={'https://playerschoicevolleyball.org/wp-content/uploads/2013/04/Female-Placeholder.jpg' as ImageSource} selectedImage={selectedImage} />
                    {/* <AdvancedImage cldImg={myImage} style={{ width: 300, height: 200 }} /> */}
                </View>
                <View style={styles.footerContainer}>
                    <View className="flex-row gap-3">
                        <View className="w-[170px] h-[68px] border-4 border-yellow-500 rounded-xl">
                            <Button moreContainerClassNames=" bg-white w-full h-full border-2 border-black-500 rounded-xl align-center justify-center " moreTextClassNames="font-semibold text-base text-center" onPress={pickImageGalleryAsync} >
                                <FontAwesome name="picture-o" size={24} color="black" />
                                <Text>  Choose from Gallery</Text>
                            </Button>
                        </View>
                        <View className="w-[170px] h-[68px] border-4 border-yellow-500 rounded-xl">
                            <Button onPress={pickImageCameraAsync} moreContainerClassNames="bg-white w-full h-full border-2 border-black-500 rounded-xl align-center justify-center " moreTextClassNames="font-semibold text-base text-center" >
                                <FontAwesome name="camera" size={24} color="black" style={{ paddingRight: 10 }} />
                                <Text>  Choose from Camera</Text>
                            </Button>
                        </View>
                    </View>
                    <Button onPress={() => imagePickerModalHandlers.open()} moreContainerClassNames="bg-transparent" moreTextClassNames=" text-white font-semibold text-xl" >Use this photo</Button>
                </View>
            </View>
            <ImagePickerModalT isShow={isShowImagePickerModal === -111} onClose={imagePickerModalHandlers.close} uploadUri={selectedImage} />
        </>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#25292e',
        alignItems: 'center',
        justifyContent: 'center',

    },
    imageContainer: {
        flex: 0.7,
        // backgroundColor: 'red'
    },
    footerContainer: {
        flex: 1 / 3,
        alignItems: 'center',
        gap: 20
    },
});