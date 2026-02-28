package org.example.gfx;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Textures {
    // 【素材の保管庫】
    // プログラムが起動したときに、この "floors" という変数に画像を読み込んで保存しておきます。
    // static がついているので、Bitmap3Dクラスから「Textures.floors」と呼ぶだけでいつでも取り出せます。
    public static Bitmap floors = loadTexture("/textures/textures.png");

    /**
     * 画像ファイルを読み込んで、自作のBitmapクラスに変換するメソッド
     */
    public static Bitmap loadTexture(String path) {
        try {
            // --- 1. 画像ファイルをJavaの世界に呼び出す ---
            // getResourceAsStream は、パソコンの中のあちこちを探すのではなく、
            // 「このゲーム（プログラム）の内部パッケージ」からファイルを探し出して読み込むための命令です。
            // ImageIO.read が、PNGなどの画像データをJavaが扱える「BufferedImage」という形に翻訳してくれます。
            BufferedImage image = ImageIO.read(Textures.class.getResourceAsStream(path));
            
            // --- 2. 私たちの3Dエンジン用の「空のキャンバス」を用意する ---
            // Java標準の画像データは複雑すぎて、3Dの猛スピードの計算（ピクセル単位の操作）には不向きです。
            // そこで、読み込んだ画像と同じ縦横サイズの、自作のシンプルなキャンバス（Bitmap）を用意します。
            Bitmap res = new Bitmap(image.getWidth(), image.getHeight());
            
            // --- 3. ピクセルデータの「一括コピー」 ---
            // Javaの画像(image)から、自作のキャンバス(res.pixels)へ、全ての色データを一気に流し込みます。
            // これで、ただの整数の配列として色を扱えるようになります。
            image.getRGB(0, 0, res.width, res.height, res.pixels, 0, res.width);
            
            // --- 4. 色データの「お掃除（ビット演算）」 ---
            // 読み込んだすべてのピクセル（点）を1つずつチェックします。
            for (int i = 0; i < res.pixels.length; i++) {
                // 【透明度のカット】
                // Javaが読み込んだ色は「0xAARRGGBB」（A=透明度、R=赤、G=緑、B=青）という状態になっています。
                // 「& 0xffffff」というハサミ（ビットマスク）を使うことで、
                // 上の2桁（AA:透明度）を切り捨てて、「0x00RRGGBB」という純粋な色データだけに掃除しています。
                res.pixels[i] = res.pixels[i] & 0xffffff;
            }
            
            // 準備が整ったキャンバスを返します
            return res;
            
        } catch (IOException e) {
            // もしファイルが見つからなかったり、読み込みに失敗した場合はエラーメッセージを出します
            e.printStackTrace();
        }
        
        // 失敗した場合は「空っぽ（null）」を返します
        return null;
    }
}
