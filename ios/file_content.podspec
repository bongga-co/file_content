#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'file_content'
  s.version          = '0.0.3'
  s.summary          = 'A new Flutter plugin to receive and share content.'
  s.description      = <<-DESC
A new Flutter plugin to receive and share content.
                       DESC
  s.homepage         = 'https://bongga.co'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Bongga' => 'info@bongga.co' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'

  s.ios.deployment_target = '9.0'
end

