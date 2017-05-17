(ns textured-quad.core
  
  (:import [com.badlogic.gdx ApplicationAdapter Gdx ApplicationListener]
           [com.badlogic.gdx.graphics GL20 GL30 Texture Mesh VertexAttribute VertexAttributes$Usage Color PerspectiveCamera Pixmap$Format]
           [com.badlogic.gdx.graphics.g3d Model ModelBatch ModelInstance Material Environment]
           [com.badlogic.gdx.graphics.g3d.environment PointLight]
           [com.badlogic.gdx.graphics.g3d.attributes ColorAttribute TextureAttribute]
           [com.badlogic.gdx.graphics.g2d SpriteBatch]
           [com.badlogic.gdx.graphics.g3d.utils ModelBuilder CameraInputController FirstPersonCameraController]
           [com.badlogic.gdx.graphics.glutils ShaderProgram FrameBuffer]
           [com.badlogic.gdx Input$Keys]
           [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [com.badlogic.gdx.math Vector2 Intersector Vector3 Matrix4]
           
           )
  (:gen-class))

(defn make-application [] 
  (let [mesh (atom nil)
        model-instances (atom nil)
        model-batch (atom nil)
        tex (atom nil)
        cam (atom nil)
        cont (atom nil)
        environment (atom nil)
        light (atom nil)
        fbo (atom nil)
        sb (atom nil)]
    (reify 
      ApplicationListener
      (create [this]
        (reset! tex (Texture. (.internal Gdx/files "brick.png")))
        (let [cam (reset! cam (doto (PerspectiveCamera. 48.262 320 240)
                                (.rotate (Vector3. 1 0 0) 85.8)
                                (#(.set (.position %) 0 0 0.41))
                                (#(set! (.near %) 0.5))))
              _ (.update cam)
              box (.createBox (ModelBuilder.)
                              1 1 0.6519
                              (Material. (into-array [(TextureAttribute/createDiffuse @tex)]))
                              (bit-or VertexAttributes$Usage/Position VertexAttributes$Usage/Normal VertexAttributes$Usage/TextureCoordinates))

              m [[[1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                  [1 1 1 1 1 1 1 1 1 1]]
                 [[1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 0 0 0 1 1 1 1]
                   [1 1 1 0 0 0 0 1 1 1]
                   [1 1 1 0 0 0 0 1 1 1]
                   [1 1 1 0 0 0 0 1 1 1]
                   [1 1 1 1 0 0 0 1 1 1]
                   [1 1 1 0 0 0 0 0 1 1]
                  [1 1 1 1 1 1 1 1 1 1]]
                 [[1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 1 1 1 1 1 1]
                   [1 1 1 1 0 0 1 1 1 1]
                   [1 1 1 1 0 0 0 1 1 1]
                   [1 1 1 1 0 0 0 1 1 1]
                   [1 1 1 1 0 0 0 1 1 1]
                   [1 1 1 1 0 0 0 1 1 1]
                   [1 1 1 0 0 0 0 0 1 1]
                  [1 1 1 1 1 1 1 1 1 1]]
                 [[1 1 1 1 1 1 1 1 1 1]
                  [1 1 1 1 1 1 1 1 1 1]
                  [1 1 1 1 1 1 1 1 1 1]
                  [1 1 1 1 1 1 1 1 1 1]
                  [1 1 1 1 1 1 1 1 1 1]
                  [1 1 1 1 1 1 1 1 1 1]
                  [1 1 1 1 1 1 1 1 1 1]
                  [1 1 1 1 1 1 1 1 1 1]
                  [1 1 1 1 1 1 1 1 1 1]
                  [1 1 1 1 1 1 1 1 1 1]]]]
          (reset! light (doto (PointLight.)
                                        (.set 1.0 1.0 1.0 0.0 0.0 0.35 4.5)))
                          
          (reset! sb (SpriteBatch.))
          (reset! fbo (FrameBuffer. Pixmap$Format/RGBA8888 160 120 true))
          (reset! environment (doto (Environment.)
                                (.add @light)
                                #_(.add (doto (PointLight.)
                                        (.set 0.6 0.6 1.0 -1.0 2.0 0.35 4.0)))
                                (.set (ColorAttribute. ColorAttribute/AmbientLight 0.01 0.01 0.01 0.1))))

          ;; todo change this to map
          (reset! model-instances
                  (for [[level l] (map vector m (range))
                        [row y] (map vector level (range))
                        [wall? x] (map vector row (range))
                        :when (= 1 wall?)]
                    (do
                      
                      (ModelInstance. box (float (- x 5)) (float (- y 5)) (- (* 0.6519 (- l 0)) (/ 0.6519 2))))))
          (reset! model-batch (ModelBatch.))

          
          (.setInputProcessor Gdx/input (reset! cont (doto (FirstPersonCameraController. cam)
                                                       #_(#(set! (.forwardKey %) Input$Keys/W ))
                                                       )))
          ) 
        nil)
      (dispose [this] nil)
      (pause [this] nil)
      (resume [this] nil)
      (render [this]
        (.begin @fbo)
        (.glClearColor Gdx/gl 0 0 0 1)
        (.glClear Gdx/gl (bit-or GL30/GL_COLOR_BUFFER_BIT GL30/GL_DEPTH_BUFFER_BIT))
        (.glEnable Gdx/gl GL30/GL_DEPTH_TEST)
        #_(.rotateAround @cam Vector3/Zero (Vector3. 0 0 1) 0.5)
        #_(.add (.position @cam) 0.0 0.01 0)
        (.update @cont)
        (.set (.position @light) (.position @cam))
        (.update @cam)
        (.begin @model-batch @cam)
        (doseq [m @model-instances]
          (.render @model-batch m @environment))
        (.end @model-batch)
        (.end @fbo)
        #_(.resize @sb 640 480)
        (.glClearColor Gdx/gl 0 0 0 1)
        (.glClear Gdx/gl (bit-or GL30/GL_COLOR_BUFFER_BIT GL30/GL_DEPTH_BUFFER_BIT))
        (.begin @sb)
        (.draw @sb (.getColorBufferTexture @fbo) (float 0.0) (float 0.0) (float 640) (float 480))
        (.end @sb))
      (resize [this width height]
        nil))))


(defn -main []
  (LwjglApplication. (make-application) "test" 640 480))
