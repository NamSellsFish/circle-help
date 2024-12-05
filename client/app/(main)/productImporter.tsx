import { Stack } from 'expo-router';
import React, { useState } from 'react';
import { BarcodeScanningResult, CameraType, CameraView, useCameraPermissions } from 'expo-camera';
import { TouchableOpacity, View, Text, StyleSheet, Button, ScrollView, Dimensions, Platform, Alert } from 'react-native';
import { TextField, Button as CustomButton, HandleResponse, ImageViewer } from '~/components';
import { FieldError, useForm } from 'react-hook-form';
import { yupResolver } from "@hookform/resolvers/yup";
import * as ImagePicker from 'expo-image-picker';
import { importProductSchema } from '~/utils';
import { ImageSource } from 'expo-image';
import { FontAwesome } from '@expo/vector-icons';
import { upload } from 'cloudinary-react-native';
import { CLD_STORAGE } from '~/constants';
import { useDebugImportProductMutation, useImportProductMutation, useProductSpecQuery } from '~/services';
import { useAppDispatch } from '~/hooks/useRedux';
import Product from '~/types/product';
import { importProductForm } from '~/store/slices/product.slice';


function ProductImporterScreen() {

    const [sku, setSku] = useState<string>('')
    const [facing, setFacing] = useState<CameraType>('back');
    const [selectedImageUri, setSelectedImageUri] = useState<string | undefined>(undefined);
    const [permission, requestPermission] = useCameraPermissions();
    const { data: productSpec, isSuccess: isGetProductSpecSuccess, isError: isGetProductSpecError } = useProductSpecQuery({ sku });
    const { imageUrl, ...formDefaultValues } = productSpec || { "packageID": "", "sku": "", "supplier": "", "name": "", "price": "", "wholesalePrice": "", "quantity": "", "expirationDate": "", "categories": "", "note": "" };
    const [importProduct, { isSuccess: isImportProductSuccess, isError: isImportProductError, isLoading: isImportProductLoading, error: importProductError  }] = useImportProductMutation()

    // TODO 1: Get Product Spec
    const handleBarcodeScanned = (data: BarcodeScanningResult) => {
        setSku(data.data);
        if (productSpec) {
            Object.keys(productSpec).forEach((key) => {
                if (key != "quantity")
                setValue(key, productSpec[key]);
            });
        }
    }


    // TODO 2: Upload image to cloudinary
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
            //setSelectedImageUri(result.assets[0].uri);
            storeImage(result.assets[0].uri)
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
            
            //setSelectedImageUri(result.assets[0].uri);
            storeImage(result.assets[0].uri)
        } else {
            alert('You did not take any photo.');
        }
    };

    const storeImage = async (imageUri: string | undefined) => {

        if (imageUri !== '') {
            await upload(CLD_STORAGE, {
                file: imageUri, options: {
                    upload_preset: 'circle_help',
                    tag: 'sample',
                    unsigned: true,
                },
                callback: async (error: any, response: any) => {
                    if (error?.message) {
                        Alert.alert(`Error: ${error?.message}. Please try again.`);
                    } else {
                        setSelectedImageUri(response.url);
                    }
                }
            })
        }
    }

    //TODO 3: Chốt submit món hàng
    //? Form Hook
    const {
        handleSubmit,
        formState: { errors: formErrors },
        setFocus,
        control,
        setValue
    } = useForm({
        resolver: yupResolver(importProductSchema),
        defaultValues: formDefaultValues,
    })
    React.useEffect(() => {
        if (isImportProductSuccess) {
            Alert.alert("Success", "Product imported successfully!");
        } else if (isImportProductError) {
            Alert.alert("Error", JSON.stringify(importProductError));
        }
    }, [isImportProductSuccess, isImportProductError, importProductError]);
    const onSubmit = handleSubmit((data: importProductForm) => {
        const {
            packageID,
            supplier,
            sku,
            name,
            price,
            wholesalePrice,
            quantity,
            expirationDate,
            categories,
            note,
        } = data;
    
        if (selectedImageUri == ''
        || selectedImageUri === undefined) {
            alert("A photo of the package must be taken first!")
            return
        }

        if (
            sku &&
            name &&
            price &&
            wholesalePrice &&
            quantity &&
            expirationDate &&
            categories &&
            packageID &&
            supplier &&
            note &&
            selectedImageUri !== ''
            && selectedImageUri
        ) {
            const body = { body: {
                sku, name, price, wholesalePrice, quantity, expirationDate, imageUrl: selectedImageUri, categories: categories.split(','), packageID, supplier, note
            }}
            
            //alert(JSON.stringify(body))


            importProduct(body);
        }
    });

    if (!permission) {
        // Camera permissions are still loading.
        return <View />;
    }

    if (!permission.granted) {
        // Camera permissions are not granted yet.
        return (
            <View style={styles.container}>
                <Text style={styles.message}>We need your permission to show the camera</Text>
                <Button onPress={requestPermission} title="grant permission" />
            </View>
        );
    }

    function toggleCameraFacing() {
        setFacing(current => (current === 'back' ? 'front' : 'back'));
    }


    return (
        <>
            <Stack.Screen
                options={{
                    title: 'Product Importer',
                    headerShown: true,
                }} />

            {/*  Handle Login Response */}
            {/* {(isGetProductSpecSuccess || isGetProductSpecError) && (
                <HandleResponse
                    isError={isGetProductSpecError}
                    isSuccess={isGetProductSpecSuccess}
                    error={'Abnormal!'}
                    //@ts-ignore
                    message={`Wellcome back ${'hihi'}`}
                    onSuccess={() => { }}
                />
            )} */}

            <ScrollView style={styles.container}>
                <Text className="w-full text-center font-black text-2xl">Scan the barcode to get the product information!</Text>
                <CameraView style={styles.camera} facing={facing} barcodeScannerSettings={{
                    barcodeTypes: ["qr", "ean13", "code128"],
                }
                }
                    onBarcodeScanned={handleBarcodeScanned}
                >
                    <View style={styles.buttonContainer}>
                        <TouchableOpacity style={styles.button} onPress={toggleCameraFacing}>
                            <Text style={styles.text}>Flip Camera</Text>
                        </TouchableOpacity>
                    </View>
                </CameraView>

                <View className="w-[100vw] px-8 py-6 space-y-4">
                    <Text className="w-full text-center font-black text-2xl">Detail</Text>
                    <View className="space-y-0">
                        <TextField
                            label='Package ID'
                            errors={formErrors.packageID as FieldError}
                            placeholder="Please enter the Package ID"
                            name="packageID"
                            //@ts-ignore
                            control={control}
                        />

                        <TextField
                            label='Supplier'
                            errors={formErrors.supplier as FieldError}
                            placeholder="Please enter the Supplier"
                            name='supplier'
                            //@ts-ignore
                            control={control}
                        />

                        <TextField
                            label='SKU'
                            errors={formErrors.sku as FieldError}
                            placeholder="Please enter the SKU"
                            name="sku"
                            //@ts-ignore
                            control={control}
                        />
                        <TextField
                            label='Product Name'
                            errors={formErrors.name as FieldError}
                            placeholder="Please enter the Product name"
                            name="name"
                            //@ts-ignore
                            control={control}
                        />
                        <TextField
                            label={`Price ${formDefaultValues.price.length == 0? "" : `(Latest recorded: ${formDefaultValues.price}$)`}`}
                            errors={formErrors.price as FieldError}
                            placeholder="Please enter the Product price"
                            name="price"
                            //@ts-ignore
                            control={control}
                        />
                        <TextField
                            label={`Wholesale Price ${formDefaultValues.wholesalePrice.length == 0? "" : `(Latest recorded: ${formDefaultValues.wholesalePrice}$)`}`}
                            errors={formErrors.wholesalePrice as FieldError}
                            placeholder="Please enter the Wholesale Price"
                            name='wholesalePrice'
                            //@ts-ignore
                            control={control}
                        />
                        <TextField
                            label={`Quantity ${formDefaultValues.quantity.length == 0 ? "" : `(Available in storage: ${formDefaultValues.quantity})`}`}
                            errors={formErrors.quantity as FieldError}
                            placeholder={`Please enter the Product quantity.`}
                            name='quantity'
                            //@ts-ignore
                            control={control}
                        />
                        <TextField
                            label='Expiration Date'
                            errors={formErrors.expirationDate as FieldError}
                            placeholder="Please enter the Product expiration date"
                            name='expirationDate'
                            //@ts-ignore
                            control={control}
                          />

                        <TextField

                             label='Categories'

                              errors={formErrors.categories as FieldError}

                               placeholder="Please enter the Product categories (comma separated)"

                                name='categories'

                            onChangeText={(text) => setValue('categories', text.split(',').map(item => item.trim()).join())}
                      //@ts-ignore
                         placeholder="Please enter the Product categories"
                            name='categories'
                            //@ts-ignore
                            control={control}
                        />
                        <TextField
                            label='Notes'
                            errors={formErrors.note as FieldError}
                            placeholder="Enter Notes here."
                            name='note'
                            //@ts-ignore
                            control={control}
                        />
                        <View className='mb-10'>
                            <Text className="text-xl text-gray-700 mb-3">Product Image</Text>
                            <View className='flex-row gap-2'>
                                <ImageViewer
                                    customStyles={{
                                        width: 200, height: 200, borderRadius: 10,
                                        resizeMode: 'contain'
                                    }}
                                    imgSource={('https://playerschoicevolleyball.org/wp-content/uploads/2013/04/Female-Placeholder.jpg') as ImageSource} selectedImage={selectedImageUri}  />
                                <View className='flex-col justify-around'>
                                    <View className="w-2/3 h-[68px] border-4 border-yellow-500 rounded-xl">
                                        <CustomButton onPress={pickImageGalleryAsync} moreContainerClassNames="bg-white w-full h-full border-2 border-black-500 rounded-xl align-center justify-center " moreTextClassNames="font-semibold text-base text-center" >
                                            <FontAwesome name="picture-o" size={24} color="black" />
                                            <Text>  Choose from Gallery</Text>
                                        </CustomButton>
                                    </View>
                                    <View className="w-2/3 h-[68px] border-4 border-yellow-500 rounded-xl">
                                        <CustomButton onPress={pickImageCameraAsync} moreContainerClassNames="bg-white w-full h-full border-2 border-black-500 rounded-xl align-center justify-center " moreTextClassNames="font-semibold text-base text-center" >
                                            <FontAwesome name="camera" size={24} color="black" style={{ paddingRight: 10 }} />
                                            <Text>  Choose from Camera</Text>
                                        </CustomButton>
                                    </View>
                                </View>
                            </View>
                        </View>

                        <CustomButton onPress={async () => await onSubmit()} moreContainerClassNames="py-3 px-8" moreTextClassNames="text-white">
                            Submit Import Product
                        </CustomButton>
                    </View>

                </View>
            </ScrollView>
        </>
    )
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        marginTop: 10,
    },
    message: {
        textAlign: 'center',
        paddingBottom: 10,
    },
    camera: {
        height: Dimensions.get('window').height / 2,
        width: '90%',
        alignSelf: 'center',
    },
    buttonContainer: {
        flex: 1,
        flexDirection: 'row',
        backgroundColor: 'transparent',
        margin: 64,
    },
    button: {
        flex: 1,
        alignSelf: 'flex-end',
        alignItems: 'center',
    },
    text: {
        fontSize: 24,
        fontWeight: 'bold',
        color: 'white',
    },
});

export default ProductImporterScreen
