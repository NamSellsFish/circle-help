import { yupResolver } from '@hookform/resolvers/yup'
import { SubmitHandler, useForm } from 'react-hook-form'
import { View } from 'react-native'

import { emailSchema } from '~/utils'
import React from 'react'
import { TextField, HandleResponse, Button, Modal } from '~/components'
import { useEditUserMutation } from '~/services'

type UserEmailModalProps = { isShow: boolean, onClose: () => void, editedData: string }

const UserNameModal = ({ isShow, onClose, editedData }: UserEmailModalProps) => {

    //? Edit User Query
    const [editUser, { isSuccess, isLoading, isError, error }] = useEditUserMutation()

    //? Form Hook
    const {
        handleSubmit,
        control,
        formState: { errors: formErrors },
    } = useForm({
        resolver: yupResolver(emailSchema),
        defaultValues: { email: editedData ? editedData : '' },
    })

    //? Handlers
    const submitHander = ({ email }: { email: string }) => {
        editUser({
            body: { email },
        })
    }

    //? Render(s)
    return (
        <>
            {/* Handle Edit User Response */}
            {(isSuccess || isError) && (
                <HandleResponse
                    isError={isError}
                    isSuccess={isSuccess}
                    error={'Error Updating User Profile'}
                    message={'User Profile Updated Successfully'}
                    onSuccess={onClose}
                />
            )}

            <Modal isShow={isShow} onClose={onClose}>
                <Modal.Content
                    onClose={onClose}
                    moreClassNames="flex flex-col px-5 py-3 bg-white rounded-lg gap-y-5 "
                >
                    <Modal.Header onClose={onClose} modalHeaderLabel='Edit User Profile' />
                    <Modal.Body>
                        <View className="flex flex-col justify-between">
                            <TextField
                                label="Email"
                                //@ts-ignore
                                control={control}
                                errors={formErrors.email}
                                name='email'
                            />

                            <View className="py-3 border-t-2 border-gray-200 lg:pb-0 ">
                                <Button onPress={handleSubmit(submitHander)} isLoading={isLoading}>
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

export default UserNameModal
