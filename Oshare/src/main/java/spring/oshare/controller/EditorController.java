package spring.oshare.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import spring.oshare.dto.BoardDTO;
import spring.oshare.dto.PhotoVO;
import spring.oshare.service.BoardService;

@Controller
@RequestMapping("editor")
public class EditorController {
	
	@Autowired
	private BoardService boardService;
	
	/**
	 *  현재 게시판(sharing, rental) 타입 정보를 가지고
	 *  제품 등록 게시판으로 이동
	 */
	@RequestMapping("insertProduct")
	public ModelAndView insertProduct(HttpServletRequest request, HttpServletResponse response , HttpSession session) throws Exception{		
		
		if(session.getAttribute("loginMemberId") == null){
			request.setAttribute("errorMsg", "로그인하고 오십시오");
			throw new Exception();
		}
		
		return new ModelAndView("list/insertProduct","boardType", request.getParameter("boardType") );
	}
	
	/**
	 * 상품수정폼
	 * */
	@RequestMapping("boardUpdate")
	public ModelAndView boardUpdate(int boardNo, HttpServletRequest request ,HttpSession session) throws Exception{
		if(session.getAttribute("loginMemberId") == null){
			request.setAttribute("errorMsg", "로그인하고 오십시오");
			throw new Exception();
		}	
		BoardDTO boardDTO = boardService.selectByBoardNo(boardNo, true);
		ModelAndView mv = new ModelAndView();
		mv.addObject("boardDTO", boardDTO);
		mv.setViewName("list/updateProduct");
		return mv;
	}
	
	/**
	 * 상품수정
	 * */
	@RequestMapping("updateSubmit")
	public String updateSubmit(BoardDTO boardDTO){
		
		int boardNo = boardService.updateBoard(boardDTO);
		
		System.out.println("bardNo Update asdasd asd : " + boardNo);
		
		return "redirect:/board/goodsDetail?boardNo="+boardDTO.getBoardNo();
	}
	
	
	
	/**
	 *  제품 등록폼에 작성된 데이터를 DB에 저장
	 */
	@RequestMapping("submit")
	public String submit(HttpServletRequest request, @RequestParam MultipartFile file , String boardType){
		// System.out.println("에디터 컨텐츠값:"+request.getParameter("editor"));
		
		/*
		 * 썸네일 첨부이미지 저장
		 */
		
		// 저장
		String defaultPath = request.getSession().getServletContext().getRealPath("/");
		//파일 기본경로 _ 상세경로
        String path = defaultPath + "resources" + File.separator + "thumbnail" + File.separator;    
        
        
		
		// 파일정보
		String fileName = file.getOriginalFilename();
		String fileName2 = file.getName();
		long size = file.getSize();
		
		String thumbnailResult = "/resources/thumbnail/"+fileName;
		
		try {
			file.transferTo(new File(path+"/"+fileName)); // 실제로 복사해주는 역할
		} catch (Exception e) {
			e.printStackTrace();
		} 	
		
		BoardDTO boardDTO = new BoardDTO(request.getParameter("title"), request.getParameter("selectCategory"), 
				request.getParameter("memberId"),Integer.parseInt(request.getParameter("sharingPrice")), request.getParameter("productState"),
				request.getParameter("boardType"), request.getParameter("editor"), thumbnailResult);
		
		int boardNo = boardService.insertBoard(boardDTO);
		
		return "redirect:/board/goodsDetail?boardNo="+boardNo;
	}
	
	//단일파일업로드
	@RequestMapping("photoUpload")
	public String photoUpload(HttpServletRequest request, PhotoVO vo){
		
	    String callback = vo.getCallback();
	    String callback_func = vo.getCallback_func();
	    String file_result = "";
	    try {
	        if(vo.getFiledata() != null && vo.getFiledata().getOriginalFilename() != null && !vo.getFiledata().getOriginalFilename().equals("")){
	            //파일이 존재하면
	            String original_name = vo.getFiledata().getOriginalFilename();
	            String ext = original_name.substring(original_name.lastIndexOf(".")+1);
	            //파일 기본경로
	            String defaultPath = request.getSession().getServletContext().getRealPath("/");
	            //파일 기본경로 _ 상세경로
	            String path = defaultPath + "resources" + File.separator + "photo_upload" + File.separator;              
	            File file = new File(path);
	            System.out.println("path:"+path);
	            //디렉토리 존재하지 않을경우 디렉토리 생성
	            if(!file.exists()) {
	                file.mkdirs();
	            }
	            //서버에 업로드 할 파일명(한글문제로 인해 원본파일은 올리지 않는것이 좋음)
	            String realname = UUID.randomUUID().toString() + "." + ext;
	        ///////////////// 서버에 파일쓰기 ///////////////// 
	            vo.getFiledata().transferTo(new File(path+realname));
	            file_result += "&bNewLine=true&sFileName="+original_name+"&sFileURL=/controller/resources/photo_upload/"+realname;
	        } else {
	            file_result += "&errstr=error";
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return "redirect:" + callback + "?callback_func="+callback_func+file_result;
	}
	
	
	//다중파일업로드
	@RequestMapping("multiplePhotoUpload")
	public void multiplePhotoUpload(HttpServletRequest request, HttpServletResponse response){
	    try {
	         //파일정보
	         String sFileInfo = "";
	         //파일명을 받는다 - 일반 원본파일명
	         String filename = request.getHeader("file-name");
	         //파일 확장자
	         String filename_ext = filename.substring(filename.lastIndexOf(".")+1);
	         //확장자를소문자로 변경
	         filename_ext = filename_ext.toLowerCase();
	         //파일 기본경로
	         String dftFilePath = request.getSession().getServletContext().getRealPath("/");
	         //파일 기본경로 _ 상세경로
	         String filePath = dftFilePath + "resources" + File.separator + "photo_upload" + File.separator;
	         File file = new File(filePath);
	         if(!file.exists()) {
	            file.mkdirs();
	         }
	         String realFileNm = "";
	         SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
	         String today= formatter.format(new java.util.Date());
	         realFileNm = today+UUID.randomUUID().toString() + filename.substring(filename.lastIndexOf("."));
	         String rlFileNm = filePath + realFileNm;
	         ///////////////// 서버에 파일쓰기 ///////////////// 
	         InputStream is = request.getInputStream();
	         OutputStream os=new FileOutputStream(rlFileNm);
	         int numRead;
	         byte b[] = new byte[Integer.parseInt(request.getHeader("file-size"))];
	         while((numRead = is.read(b,0,b.length)) != -1){
	            os.write(b,0,numRead);
	         }
	         if(is != null) {
	            is.close();
	         }
	         os.flush();
	         os.close();
	         ///////////////// 서버에 파일쓰기 /////////////////
	         // 정보 출력
	         sFileInfo += "&bNewLine=true";
	         // img 태그의 title 속성을 원본파일명으로 적용시켜주기 위함
	         sFileInfo += "&sFileName="+ filename;;
	         sFileInfo += "&sFileURL="+"/controller/resources/photo_upload/"+realFileNm;
	         PrintWriter print = response.getWriter();
	         print.print(sFileInfo);
	         print.flush();
	         print.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
}
