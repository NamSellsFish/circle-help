import { Image, ImageProps } from 'expo-image'
import { StyleProp, View, ViewStyle } from 'react-native'
import { blurDataURL } from '~/constants'


type ResponsiveImageProps = ImageProps & { dimensions?: string, alt: string, style?: StyleProp<ViewStyle> }



export default function ResponsiveImage({ dimensions, alt, style, ...rest }: ResponsiveImageProps) {

  //? Render(s)
  return (

    <View style={style} className={dimensions}>
      <Image
        alt={alt}
        style={{ width: '100%', height: '100%', }}
        placeholder={blurDataURL}
        transition={200}
        {...rest}
      />
    </View>

  )
}
