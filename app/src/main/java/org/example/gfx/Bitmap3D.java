package org.example.gfx;

public class Bitmap3D extends Bitmap {
    private double fov = height;

    public Bitmap3D(int width, int height) {
        super(width, height);
    }

    int t;
    double xCam = 0;
    double yCam = 0;
    double zCam = 4;
    double rot = 0;

    public void render() {
        t++;
        // --- 1. カメラの状態（パラメータ）の更新 ---
        xCam = t / 100.0; 
        yCam = t / 100.0; 
        zCam = Math.sin(t / 500.0); // 上下動
        rot = t / 1000.0;           // 回転角（θ）

        // 回転行列に使う準備（一度計算して使い回す）
        double rSin = Math.sin(rot);
        double rCos = Math.cos(rot);

        // --- 2. 画面を上から下へ（y軸）スキャン開始 ---
        for (int y = 0; y < height; y++) {
            
            // 画面中央からの「高さのズレ」を計算
            double yd = (y - (height / 2)) / fov;

            // 【距離の逆算】
            // 遠近法では「距離に反比例して小さく見える」ので、
            // 逆に「高さ(yd)で割る」ことで、その行が指す「地面までの距離(zd)」を出します。
            double zd = (6 + zCam) / yd;
            
            // 地平線より上の処理（空を地面の鏡合わせとして計算）
            if (yd < 0) {
                zd = (6 - zCam) / -yd;
            }

            // --- 3. その行を左から右へ（x軸）スキャン開始 ---
            for (int x = 0; x < width; x++) {
                
                // 画面中央からの「横のズレ」を計算
                double xd = (x - (width / 2)) / fov;
                
                // 【遠近法の適用】
                // 遠く（zdが大きい）ほど、一歩の歩幅を大きくして広い範囲をスキャンします。
                xd *= zd;

                // 【回転行列による座標変換】
                // (xd, zd) という「カメラから見た相対的な位置」を
                // 回転行列を使って「地図上の実際の向き」へ回転させ、カメラ位置(xCam, yCam)を足します。
                // 公式: x' = x*cosθ - y*sinθ / y' = x*sinθ + y*cosθ
                int xPix = (int) (xd * rCos - zd * rSin + xCam);
                int yPix = (int) (xd * rSin + zd * rCos + yCam);

                // --- 4. 描画色の決定 ---
                // 計算した地図上の住所（xPix, yPix）を使って色を塗ります。
                // ここではビット演算を使って「16マスおきの市松模様」を作っています。
                pixels[x + y * width] = ((yPix & 15) * 16) << 8 | ((xPix & 15) * 16);
            }
        }
    }
}
