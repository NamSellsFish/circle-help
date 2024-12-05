import { yupResolver } from '@hookform/resolvers/yup'
import { SubmitHandler, useForm } from 'react-hook-form'
import { View } from 'react-native'

import { nameSchema } from '~/utils'
import React from 'react'
import { TextField, HandleResponse, Button, Modal } from '~/components'
import { useEditUserMutation } from '~/services'

type UserNameModalProps = { isShow: boolean, onClose: () => void, editedData: string }

const UserNameModal = ({ isShow, onClose, editedData }: UserNameModalProps) => {

    //? Edit User Query
    const [editUser, { isSuccess, isLoading, isError, error }] = useEditUserMutation()

    //? Form Hook
    const {
        handleSubmit,
        control,
        formState: { errors: formErrors },
    } = useForm({
        resolver: yupResolver(nameSchema),
        defaultValues: { username: editedData ? editedData : '' },
    })

    //? Handlers
    const submitHander = ({ username }: { username: string }) => {
        editUser({
            body: { username },
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
                    error={`${JSON.stringify(error?.error)}`}
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
                                label="Username"
                                //@ts-ignore
                                control={control}
                                errors={formErrors.username}
                                name='username'
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
