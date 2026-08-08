import SwiftUI

struct NavigationBackgroundTest: View {
    var body: some View {
        NavigationStack {
            ZStack {

                Image("AppBackground")
                    .resizable()
                    .scaledToFill()
                    .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 20) {

                        Text("Hello")
                            .font(.largeTitle)
                            .foregroundStyle(.white)

                        RoundedRectangle(cornerRadius: 20)
                            .fill(.white.opacity(0.2))
                            .frame(height: 200)

                        RoundedRectangle(cornerRadius: 20)
                            .fill(.white.opacity(0.2))
                            .frame(height: 200)
                    }
                    .padding()
                }
            }
            .navigationTitle("Test")
        }
    }
}

#Preview {
    NavigationBackgroundTest()
}
