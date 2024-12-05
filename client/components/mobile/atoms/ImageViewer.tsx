import { StyleSheet } from "react-native";
import { Image, type ImageSource } from "expo-image";

type Props = {
    imgSource?: ImageSource;
    selectedImage?: string;
    customStyles?: object;

};

export default function ImageViewer({ imgSource, selectedImage, customStyles }: Props) {
    const imageSource = selectedImage ? { uri: selectedImage } : imgSource;

    return <Image source={imageSource} style={customStyles ? customStyles : styles.image} />;
}

const styles = StyleSheet.create({
    image: {
        width: 320,
        height: 440,
        borderRadius: 18,
        resizeMode: 'contain',
    },
});
