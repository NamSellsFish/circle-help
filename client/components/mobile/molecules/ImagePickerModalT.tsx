import { View, Text, Platform } from 'react-native'
import * as Location from 'expo-location'
import React, { useState } from 'react'
import { HandleResponse, Button, Modal } from '~/components'
import { upload } from 'cloudinary-react-native'
import { CLD_STORAGE } from '~/constants'
import { useAppDispatch, useAppSelector, useDate } from '~/hooks'
import { updateAttendanceInfoItem } from '~/store'
import { useCheckAttendanceMutation } from '~/services'
import { router, useRouter } from 'expo-router'
import { getAttendance } from '~/store'
import { initialAttendanceState } from '~/store'
import { dateToDateOnlyString, dateToTimeOnlyString } from '~/store'


type ImagePickerModalProps = { isShow: boolean, onClose: () => void, uploadUri: string | undefined }

const ImagePickerModalT = ({ isShow, onClose, uploadUri }: ImagePickerModalProps) => {
    const dispatch = useAppDispatch()
    const [uploadStatus, setImageUploadStatus] = useState('isUploading');
    const [checkAttendance, { data, isUninitialized, isSuccess, isError, isLoading, error: checkAttendanceError }] = useCheckAttendanceMutation()
    
    const {
        type,
        currDate,
        punchInTime,
        punchOutTime,   
    } = getAttendance()

    //alert(JSON.stringify(getAttendance()))

    const componentDidMount = async () => {
        if (Platform.OS !== "web") {
            const {
                status,
            } = await Location.requestForegroundPermissionsAsync();
            if (status !== "granted") {
                alert("Sorry, we need access location permissions to make this work!");
            }
        }
    }

    async function getCurrentLocation() {
        await componentDidMount();
        let location = await Location.getCurrentPositionAsync({});
        return location;
    }

    const realTimeDate = useDate()

    const submitHandler = async (pickerResult: string | undefined) => {

        // TODO: Lấy tọa độ hiện tại
        const currLocation = await getCurrentLocation();

        setImageUploadStatus('isUploading');

        if (pickerResult !== '') {
            await upload(CLD_STORAGE, {
                file: pickerResult, options: {
                    upload_preset: 'circle_help',
                    tag: 'sample',
                    unsigned: true,
                }, callback: async (error: any, response: any) => {
                    if (error?.message) {
                        setImageUploadStatus(`Error: ${error?.message}. Please try again.`);
                    } else {

                        const newType = type === undefined ? 'PunchIn' : 'PunchOut'

                        const request = {
                            body: {
                                type: newType,
                                currDate: dateToDateOnlyString(new Date()),
                                punchInTime: punchInTime === initialAttendanceState.punchInTime ? realTimeDate.time: punchInTime,
                                punchOutTime: newType === 'PunchOut' ? realTimeDate.time : initialAttendanceState.punchOutTime,
                                imageUrl: response.url,
                                currLatitude: currLocation.coords.latitude,
                                currLongitude: currLocation.coords.longitude
                            }
                        }

                        // alert("Request:" + JSON.stringify(request));

                        const result = await checkAttendance(request).unwrap()
                        
                        // // await new Promise(r => setTimeout(r, 300));
                        // alert("isSuccess: " + isSuccess
                        //     +"\nisLoading: " + isLoading
                        //     +"\nisError: " + isError
                        //     +"\nisUninitialized: " + isUninitialized
                        // );

                        if (result) {
                            const data = result
                            const type = data.type;
                            const dispatchData = { ...data, type: type === 'PunchIn' || type === 'Late' ? 'PunchIn' : type === 'FullAttendance' || type === 'Overtime' || type === 'EarlyLeave' ? 'PunchOut' : undefined };
                            //alert("Dispatch Data:" + JSON.stringify(dispatchData));
                            dispatch(updateAttendanceInfoItem(dispatchData));
                            setImageUploadStatus('Check attendance successfully');
                        }
                        isError && setImageUploadStatus(`Error: ${JSON.stringify(checkAttendanceError)}. Please try again.`);
                        onClose(); 
                        router.back()
                    }
                }
            });
        }
    }

    const handleOnSuccess = () => {
        onClose();
        router.back();
    }




    //? Render(s)
    return (
        <>
            {/* Handle Edit User Response */}
            {(uploadStatus !== 'isUploading') && (
                <HandleResponse
                    isError={uploadStatus.substring(0, 5) === 'Error'}
                    isSuccess={uploadStatus.substring(0, 5) !== 'Error'}
                    error={`${uploadStatus}`}
                    message={`${uploadStatus}`}
                    onSuccess={handleOnSuccess}
                />
            )}

            <Modal isShow={isShow} onClose={onClose}>
                <Modal.Content
                    onClose={onClose}
                    moreClassNames="flex flex-col px-5 py-3 bg-white rounded-lg gap-y-5 "
                >
                    <Modal.Header onClose={onClose} modalHeaderLabel='Confirm Evidence' />
                    <Modal.Body>
                        <View className="flex flex-col justify-between">
                            <Text className='text-center'>Are you sure you want to upload this proof? THIS ACTION CAN NOT BE UNDO!!</Text>

                            <View className="flex-row justify-around py-3 lg:pb-0 ">
                                <Button moreContainerClassNames='bg-red-500 px-3 py-3' moreTextClassNames='text-xl font-semibold' onPress={onClose}>
                                    Cancel
                                </Button>
                                <Button style={{ backgroundColor: 'green' }} moreContainerClassNames='px-3 py-3' moreTextClassNames='text-xl font-semibold' onPress={async () => await submitHandler(uploadUri)}>
                                    Submit
                                </Button>
                            </View>
                        </View>
                    </Modal.Body>
                </Modal.Content>
            </Modal>
        </>
    )
}

export default ImagePickerModalT
