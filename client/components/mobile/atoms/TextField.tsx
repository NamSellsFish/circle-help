import React from 'react'
import { Control, FieldError, useController } from 'react-hook-form'
import { Text, TextInput, TextInputProps, View } from 'react-native'
import DisplayError from './DisplayError'

type TextFieldProps = {
    errors: FieldError | undefined,
    label?: string,
    name: "email" | "password",
    control: Control<{ email: string; password: string; }, any>,
    type?: string,
} & TextInputProps

export default function TextField({ label, errors, name, type = 'text', control, ...inputProps }: TextFieldProps) {

    //? Form Hook
    const { field } = useController({ name, control, rules: { required: true } })
    //? Handlers
    const onChangeHandler = (value: string) => {
        const inputValue = value

        if (type === 'number' && inputValue.length !== 0) {
            field.onChange(parseInt(inputValue))
        } else {
            field.onChange(inputValue)
        }
    }

    //? Render(s)
    return (
        <View className="w-full">
            {label && <Text className="text-xs text-gray-700 ">{label}</Text>}
            <TextInput
                className="w-full px-3 py-2.5 transition-colors border border-gray-200 rounded-md outline-none bg-zinc-50/30 focus:border-blue-600 leading-none"
                id={name}
                value={field?.value}
                // ISSUE: Nếu comment thế này mà không bị lỗi gì thì bỏ luôn name={field.name}
                onBlur={field.onBlur}
                onChangeText={onChangeHandler}
                ref={field.ref}
                {...inputProps}
                style={{ marginTop: 10 }}
            />
            <DisplayError errors={errors} />
        </View>
    )
}